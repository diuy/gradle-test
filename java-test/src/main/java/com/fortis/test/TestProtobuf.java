package com.fortis.test;

import com.fortis.test.user.Address;
import com.fortis.test.user.People;
import com.google.protobuf.InvalidProtocolBufferException;

public class TestProtobuf {
    public static void main(String[] args) throws InvalidProtocolBufferException {


        People people = People.newBuilder().setId(1).setName("xc").addChildren(1).addChildren(2)
                .addAddress(Address.newBuilder().setCity("cd").setRegion("jn"))
                .addAddress( Address.newBuilder().setCity("cd").setRegion("px")).build();
        byte[] bytes = people.toByteArray();

        People people1 = People.parseFrom(bytes);
        System.out.println(people1.toString());
    }
}
