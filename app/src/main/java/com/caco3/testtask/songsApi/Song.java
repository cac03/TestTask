package com.caco3.testtask.songsApi;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An immutable class representing song model
 */
public final class Song {
    private final String author;
    private final String name;
    private final long id;
    private final int version;

    public Song(String author, String name, long id, int version) {
        this.author = author;
        this.name = name;
        this.id = id;
        this.version = version;
    }


    public String getAuthor() {
        return author;
    }


    public String getName() {
        return name;
    }


    public long getId() {
        return id;
    }


    public int getVersion() {
        return version;
    }



    /**
     * Static factory method. Creates new {@link Song} from json
     * @param json to create from
     * @return {@link Song} created from provided json
     * @throws JSONException if invalid json was provided
     */
    /* package */static Song fromJson(JSONObject json) throws JSONException{
        String name = json.getString("label");
        String author = json.getString("author");
        long id = json.getLong("id");
        int version = json.getInt("version");

        return new Song(author, name, id, version);
    }

    // generated by ide
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (id != song.id) return false;
        if (version != song.version) return false;
        if (!author.equals(song.author)) return false;
        return name.equals(song.name);

    }

    // generated by ide
    @Override
    public int hashCode() {
        int result = author.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + version;
        return result;
    }

    // generated by ide
    @Override
    public String toString() {
        return "Song{" +
                "author='" + author + '\'' +
                ", name='" + name + '\'' +
                ", id=" + id +
                ", version=" + version +
                '}';
    }
}
