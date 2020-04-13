package it.polimi.tiw.utility;

public interface JsonConverter<T> {
    String convertToJson(T obj);
}
