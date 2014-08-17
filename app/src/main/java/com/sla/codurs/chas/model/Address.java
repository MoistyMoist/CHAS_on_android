package com.sla.codurs.chas.model;

import java.util.ArrayList;

/**
 * Created by Moistyburger on 9/7/14.
 */
public class Address {

    public int hashCode;
    private double x;
    private double y;
    private String title;
    private String iconURL;
    private String addesss;
    private String contactList;

    public Address() {
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
