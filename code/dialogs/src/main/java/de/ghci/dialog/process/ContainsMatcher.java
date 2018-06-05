package de.ghci.dialog.process;

/**
 * @author Dominik
 */
public class ContainsMatcher {

    public boolean matches(String keyword, String match) {
        if(keyword == null || match == null) {
            return false;
        }
        return simplify(match).contains(simplify(keyword));
    }

    private String simplify(String string) {
        if(string == null) {
            string = "";
        }
        return string.toLowerCase().trim().replace(".", "").replace(",","").replace(":","").replace(";","");
    }

    public boolean matchesWord(String keyword, String match) {
        if(keyword == null || match == null) {
            return false;
        }
        String[] tokens = match.split(" ");
        for(String token : tokens) {
            if(simplify(token).equals(simplify(keyword))) {
                return true;
            }
        }
        return false;
    }
}
