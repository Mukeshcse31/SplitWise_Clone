package com.android.app.splitwise_clone.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Group implements Parcelable, Cloneable {

    private String name;
    private String photoUrl = "";
    private String owner;
    private Float totalAmount;
    private Map<String, SingleBalance> members = new HashMap<>();
    private Map<String, SingleBalance> nonMembers = new HashMap<>();
    private Map<String, Expense> expenses = new HashMap<>();
    private Map<String, Expense> archivedExpenses = new HashMap<>();

    public Group() {
    }

    public Group(String name) {
        this.name = name;
        totalAmount = .0f;
    }

    public Group(String name, Map<String, SingleBalance> members) {
        this.name = name;
        this.members = members;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    protected Group(Parcel in) {

        name = in.readString();
        photoUrl = in.readString();
        owner = in.readString();
        totalAmount = in.readFloat();
        members = new HashMap<String, SingleBalance>();
        in.readMap(members, SingleBalance.class.getClassLoader());
        nonMembers = new HashMap<String, SingleBalance>();
        in.readMap(nonMembers, SingleBalance.class.getClassLoader());
        expenses = new HashMap<String, Expense>();
        in.readMap(expenses, Expense.class.getClassLoader());
        archivedExpenses = new HashMap<String, Expense>();
        in.readMap(archivedExpenses, Expense.class.getClassLoader());
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, SingleBalance> getMembers() {
        return members;
    }

    public void setMembers(Map<String, SingleBalance> members) {
        this.members = members;
    }

    public void addMember(String memberName, SingleBalance sb) {
        this.members.put(memberName, sb);
    }

    public Map<String, SingleBalance> getNonMembers() {
        return nonMembers;
    }

    public void setNonMembers(Map<String, SingleBalance> nonMembers) {
        this.nonMembers = nonMembers;
    }

    public void addNonMember(String memberName, SingleBalance sb) {
        this.nonMembers.put(memberName, sb);
    }

    public Map<String, Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(Map<String, Expense> expenses) {
        this.expenses = expenses;
    }

    public Map<String, Expense> getArchivedExpenses() {
        return archivedExpenses;
    }

    public void setArchivedExpenses(Map<String, Expense> archivedExpenses) {
        this.archivedExpenses = archivedExpenses;
    }

    public void removeMember(String memberName) {
        members.remove(memberName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(photoUrl);
        dest.writeString(owner);
        dest.writeFloat(totalAmount);
        dest.writeMap(members);
        dest.writeMap(nonMembers);
        dest.writeMap(expenses);
        dest.writeMap(archivedExpenses);
    }

}
