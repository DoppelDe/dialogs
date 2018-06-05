package de.ghci.dialog.model.soccer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import de.ghci.dialog.model.Entity;

import java.util.Objects;

/**
 * @author Dominik
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team implements Entity {

    private static final long serialVersionUID = 2645728645639589310L;
    private int id;
    private String code;
    private String name;
    private String shortName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id == team.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String getLabel() {
        if(!Strings.isNullOrEmpty(shortName)) {
            return shortName;
        } else {
            return name;
        }
    }
}
