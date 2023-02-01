package models;

public class Child1 extends Parent1 {
    int a = 10;
    int b = 20;
    int c = 50;
    int d = 60;
    public static void main(String[] aa) {
        Child1 cobj = new Child1();
        System.out.println(cobj.a);
        System.out.println(cobj.b);
        //System.out.println(super.c);
        //System.out.println(super.d);
        Parent1 pcobj = new Child1();
        //System.out.println(pcobj.a);
        //System.out.println(pcobj.b);
        System.out.println(pcobj.c);
        System.out.println(pcobj.d);
        Parent1 pobj = new Parent1();
        //System.out.println(pobj.a);
        //System.out.println(pobj.b);
        System.out.println(pobj.c);
        System.out.println(pobj.d);
        //Child1 cobj = new Parent1();
    }
}
