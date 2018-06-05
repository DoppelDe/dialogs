package de.ghci.dialog.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.ghci.dialog.io.soccer.PersonCollectionClassAdapter;
import de.ghci.dialog.model.soccer.Person;

import java.util.*;

/**
 * @author Dominik
 */
public class DialogsGsonBuilder {

    private static Gson gson;

    public static Gson getGson() {
        if(gson == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(new TypeToken<Collection<Person>>() {}.getRawType(), new PersonCollectionClassAdapter());
//            builder.registerTypeAdapter(new TypeToken<Set<Person>>() {}.getRawType(), new PersonCollectionClassAdapter());
//            builder.registerTypeAdapter(new TypeToken<List<Person>>() {}.getRawType(), new PersonCollectionClassAdapter());
//            builder.registerTypeAdapter(new TypeToken<Person>() {}.getType(), new PersonClassAdapter());
            gson = builder.create();
        }
        return gson;
    }

}
