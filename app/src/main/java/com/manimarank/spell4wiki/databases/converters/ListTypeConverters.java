package com.manimarank.spell4wiki.databases.converters;

public class GithubTypeConverters {
    
    Gson gson = new Gson();
    
    @TypeConverter
    public static List<SomeObject> stringToSomeObjectList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<SomeObject>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String someObjectListToString(List<SomeObject> someObjects) {
        return gson.toJson(someObjects);
    }
}