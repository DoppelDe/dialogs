package de.ghci.dialog.io.soccer;

import com.google.gson.*;
import com.sun.deploy.net.proxy.PACFunctionsImpl;
import de.ghci.dialog.model.soccer.Person;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Dominik
 */
public class PersonCollectionClassAdapter implements JsonDeserializer<Collection<Person>> {

    public static final String PERSON_PACKAGE = "de.ghci.dialog.model.soccer.";

    @Override
    public Collection<Person> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        Collection<Person> collection = new ArrayList<>();
        JsonArray ja = jsonElement.getAsJsonArray();

        for (JsonElement je : ja) {
            JsonObject jsonObject = je.getAsJsonObject();
            try {
                String typeString = jsonObject.get("type").getAsString();
                try {
                    collection.add(context.deserialize(jsonObject, Class.forName(PERSON_PACKAGE + typeString)));
                } catch (ClassNotFoundException cnfe) {
                    System.err.println("Unknown element type: " + type);
                    cnfe.printStackTrace();
                }
            } catch (NullPointerException e) {
                System.out.println("Obj: " + jsonObject);
                throw e;
            }

        }
        return collection;
    }
}
