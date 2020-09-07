package spring.newtest;

/**
 * @author rafa gao
 */


public class Son extends Father {


    public Son() {
        System.out.println("son is constructed");
    }

    public static void main(String[] args) {
        new Son("son");
    }

    public Son(String s) {
        System.out.println("Son [String] is Constructed");
    }
}
