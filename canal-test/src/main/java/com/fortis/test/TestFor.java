package test;

public class TestFor {

    public static int a(){
        return 0;
    }

    public static int b(){
        return 1;
    }

    public static void main(String[] args){
//        for (int i=a(),j=b();;){
//            System.out.printf("%d,%d\n",i,j);
//        }
        int k = 1;
        int i = k;
        k=2;
        if(i!=(i=k)){
            System.out.println(i);
        }


    }
}
