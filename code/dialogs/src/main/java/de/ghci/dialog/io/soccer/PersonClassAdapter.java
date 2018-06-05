package de.ghci.dialog.io.soccer;

import com.google.gson.*;
import de.ghci.dialog.model.soccer.Person;

import java.lang.reflect.Type;

public class PersonClassAdapter implements JsonDeserializer<Person> {

    @Override
    public Person deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement element = jsonObject.get("properties");

        String type = jsonObject.get("type").getAsString();
        try {
            String thepackage = "de.ghci.dialog.model.soccer.";
            return context.deserialize(element, Class.forName(thepackage + type));
        } catch (ClassNotFoundException cnfe) {
            throw new JsonParseException("Unknown element type: " + type, cnfe);
        }
    }
}