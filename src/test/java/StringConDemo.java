public class StringConDemo {

    public static void main(String[] arg){
        String s = "abc";
        String s3 = "abc";
        String s1 = new String("abc");
        String s2 = new String("abc");
        System.out.println(s.equals(s1));
        System.out.println(s1 == s2);
        System.out.println(s == s3);
    }
}
