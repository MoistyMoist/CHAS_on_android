package com.sla.codurs.chas.model;

import java.util.ArrayList;

/**
 * Created by Moistyburger on 9/7/14.
 */
public class Chas {
    public int hashCode;
    private String title;
    private String description;
    private String address;
    private String URL;
    private double x;
    private double y;
    private String iconURL;

    public Chas() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
    public void setX(double x){
        this.x=x;
    }

    public double getX(){
        return this.x;
    }

    public void setY(double y){
        this.y=y;
    }

    public double getY(){
        return this.y;
    }

    public void setTitle(String title){
        this.title=title;
    }

    public String getTitle(){
        return this.title;
    }

    public void setIconURL(String iconURL){
        this.iconURL=iconURL;
    }

    public String getIconURL(){
        return this.iconURL;
    }

}
