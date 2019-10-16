public class Main {
    public static void main(String[] args) {
        Serialiser s = new Serialiser();
        Person person = new Person("Vladimir", "RUS", 19);
        s.serialize(person);
    }
}