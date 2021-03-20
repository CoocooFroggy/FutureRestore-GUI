public class Main {
    public static void main(String[] args) {
        //If they are not java 8, return
        if (!JavaChecker.checkJava8())
            //Closes the program
            System.exit(0);
        MainMenu.main(new String[0]);
    }
}
