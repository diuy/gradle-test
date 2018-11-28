package com.fortis.test

import com.fortis.drug.proto.DrugProto.DrugSell
import com.fortis.drug.proto.DrugProto.DrugSells
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisConnectionException

public class CreateProto {

    String redisUrl = "redis://172.20.11.114:6379/10"
    String mysqlUrl = 'jdbc:mysql://172.20.1.233/drugdb?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&serverTimezone=GMT&tinyInt1isBit=false'
    String user = 'root'
    String password = 'Pass1234'
    String driver = 'com.mysql.jdbc.Driver'


    Sql mysql;//= Sql.newInstance(mysqlUrl, user, password, driver)
    Jedis jedis;//= new Jedis(new URI(redisUrl))

    // Hash
    // key drug:store:sell:store_id
    // value drug_id->sell detail (json)
    def KEY_STORE_SELL = "drug:store:sell:"

    // Hash
    // key drug:chain:sell:chain_id
    // value drug_id->sell detail (json)
    def KEY_CHAIN_SELL = "drug:chain:sell:"

    // Hash
    // key drug:base:
    // value drug_id->drug detail (json)
    def KEY_BASE = "drug:base:"

    // Set
    // key drug:cat:cat_id
    // values drug_id
    def KEY_DRUG_CAT = "drug:cat:"

    // Hash
    // key drug:type:drug_id
    // values drug_id->1
    def KEY_DRUG_TYPE = "drug:type:"

    def drugMax = 50000//药品数量
    def storeDrugMax = 3000//每个药店的药品数量
    def storeMax = 10000//药店数量


    def sql1 = '''SELECT cate_id FROM `b_drug_category` WHERE sup_id IS NOT NULL'''
    def sql2 = '''SELECT drug_id FROM b_drug WHERE cate_id1 = ?'''
    def sql3 = '''SELECT * FROM b_chain_drug WHERE chain_id =? ORDER BY drug_id'''
    def sql4 = '''SELECT chain_id FROM b_chain_drug GROUP BY chain_id'''

    def sql5 = '''SELECT drug_id FROM b_drug WHERE drug_id > ? AND `type`='中成药' LIMIT 2000'''

    def sql6 = '''SELECT * FROM b_store_drug WHERE store_id =? ORDER BY drug_id'''
    def sql7 = '''SELECT store_id FROM b_store_drug GROUP BY store_id'''

    public void createCategory() {
        def categories = mysql.rows(sql1)
        def pipeline = jedis.pipelined()
        for (def category : categories) {
            def cate_id = category.get("cate_id") as Long
            def ret = mysql.rows(sql2, cate_id)
            def ids = []
            for (def id : ret) {
                ids.add(id.get("drug_id"))
            }

            pipeline.sadd(KEY_DRUG_CAT + cate_id, ids as String[])
        }
        pipeline.sync()
    }

    public void createType() {
        long id = 0
        while (true) {
            def drugs = mysql.rows(sql5, id)
            if (drugs.isEmpty())
                break
            def pipeline = jedis.pipelined()
            for (def drug : drugs) {
                id = drug.get("drug_id") as Long
                pipeline.hset(KEY_DRUG_TYPE, id as String, "\u0008\u0001")
            }
            pipeline.sync()
        }
    }

    private void createSell(List<GroovyRowResult> sells, Long id, int type) {
        Map<Long, List<Map>> drugMap = [:]
        for (def sell : sells) {
            Long drugId = sell.get("drug_id") as Long
            def s = drugMap.get(drugId)
            if (s == null) {
                s = new ArrayList<Map>()
                drugMap.put(drugId, s)
            } else {
                if (type == 0)
                    println "drug id multiple  store_id:${id}, drug_id:${drugId}"
                else
                    println "drug id multiple  chain_id:${id}, drug_id:${drugId}"
            }
            s.add(sell)
        }
        def pipeline = jedis.pipelined()
        for (def m : drugMap) {
            def drugs = m.value
            def bs = DrugSells.newBuilder()
            for (def drug : drugs) {
                def b = DrugSell.newBuilder()
                        .setSellId(drug.get("id") as Long)
                        .setSpec(drug.get("spec")==null?"":drug.get("spec") as String)
                        .setPrice(drug.get("price") as Float)
                        .setRcmd(drug.get("is_rcmd") as Integer)
                        .setControll(drug.get("is_controllpin") as Integer)
                        .setType(drug.get("drug_type") as Integer)
                        .setHighSale(drug.get("is_high_sale") as Integer)
                bs.addDrugSell(b)
            }
            DrugSells drugSells = bs.build()
            byte[] k
            if (type == 0)
                k = (KEY_STORE_SELL + id).getBytes()
            else
                k = (KEY_CHAIN_SELL + id).getBytes()

            def ck = (m.key as String).getBytes()
//            if (id == 3 && m.key == 1240) {
//                println BaseEncoding.base16().encode(drugSells.toByteArray())
//            }
            pipeline.hset(k, ck, drugSells.toByteArray())
        }
        pipeline.sync()
    }

    public void createChainSell() {
        def r1 = mysql.rows(sql4)
        for (def r : r1) {
            def chainId = r.get("chain_id") as Long
            def r2 = mysql.rows(sql3, chainId)
            try{
                createSell(r2, chainId, 1)
            }catch (JedisConnectionException e){
                println "jedis 超时"+e.getMessage()
                jedis = new Jedis(new URI(redisUrl))
                createSell(r2, chainId, 1)
            }
        }
    }

    public void createStoreSell() {
        def r1 = mysql.rows(sql7)
        for (def r : r1) {
            def store_id = r.get("store_id") as Long
            def r2 = mysql.rows(sql6, store_id)
            try{
                createSell(r2, store_id, 0)
            }catch (JedisConnectionException e){
                println "jedis 超时"+e.getMessage()
                jedis = new Jedis(new URI(redisUrl))
                createSell(r2, store_id, 0)
            }
        }
    }

    private void readConfig() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")
        if (inputStream == null) {
            println "read config failed"
            return
        }
        Properties properties = new Properties()
        properties.load(inputStream)
        redisUrl = properties.get("redisUrl", redisUrl)
        mysqlUrl = properties.get("mysqlUrl", mysqlUrl)
        user = properties.get("user", user)
        password = properties.get("password", password)
        println "redisUrl  :${redisUrl}"
        println "mysqlUrl  :${mysqlUrl}"
        println "user  :${user}"
        println "password  :${password}"

        mysql = Sql.newInstance(mysqlUrl, user, password, driver)
        jedis = new Jedis(new URI(redisUrl))
    }

    public static void main(String[] args) {
        CreateProto createProto = new CreateProto()
        createProto.readConfig()
        createProto.createType()
        createProto.createCategory()
        createProto.createChainSell()
        createProto.createStoreSell()

    }


}
