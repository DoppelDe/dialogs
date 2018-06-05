package de.ghci.dialog.io.soccer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import de.ghci.dialog.io.DialogsGsonBuilder;
import de.ghci.dialog.model.soccer.Person;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @author Dominik
 */
public class PersonLoader {

    private static final String FILE_PATH = "C:\\Users\\Dominik\\Documents\\studium_master\\masterarbeit\\workspace\\dialogs\\src\\main\\resources\\persons.json";
    private static Collection<Person> people;

    public static void storePersons(Collection<Person> persons) throws IOException {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            DialogsGsonBuilder.getGson().toJson(persons, writer);
        }
    }

    public static Collection<Person> loadPersons() throws IOException {
        if(people != null && !people.isEmpty()) {
            return people;
        }
        Type type = new TypeToken<Collection<Person>>() {}.getType();
        try(JsonReader reader = new JsonReader(new FileReader(FILE_PATH))) {
            people = DialogsGsonBuilder.getGson().fromJson(reader, type);
            return people;
        }
    }
}
