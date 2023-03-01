package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PutMapping;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashMap<String, User> userMobileMap;
    private HashMap<Integer, Message> messageMap;
    private int customGroupCount;
    private int messageId; // last message id

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobileMap = new HashMap<>();
        this.messageMap = new HashMap<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        if(userMobileMap.containsKey(mobile)) throw new Exception("User already exists");
        else{
            userMobileMap.put(mobile, new User(name, mobile));
            return "SUCCESS";
        }
    }

    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.

        int usersCount = users.size();

        if(usersCount <= 2) {
            Group newGroup = new Group(users.get(1).getName(), usersCount);
            // updating group user map
            groupUserMap.put(newGroup, users);
            // updating admin
            adminMap.put(newGroup, users.get(0));

            return newGroup;
        } else {
            customGroupCount++;
            Group newGroup = new Group("Group " + customGroupCount, usersCount);
            // updating group user map
            groupUserMap.put(newGroup, users);
            // updating admin
            adminMap.put(newGroup, users.get(0));

            return newGroup;
        }
    }

    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        messageId++;
        Message newMessage = new Message(messageId, content);

        messageMap.put(messageId, newMessage);

        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.

        if(groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }

        List<User> groupUsers = groupUserMap.get(group);
        if(!groupUsers.contains(sender)) {
            throw new Exception("You are not allowed to send message");
        }

        senderMap.put(message, sender);
        List<Message> messages = groupMessageMap.getOrDefault(group, new ArrayList<>());
        messages.add(message);
        groupMessageMap.put(group, messages);

        return messages.size();

    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.

        if(groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        } else {
            List<User> groupUsers = groupUserMap.get(group);

            if(!groupUsers.contains(user)) {
                throw new Exception("User is not a participant");
            } else {
                if(adminMap.get(group) != approver || !groupUsers.contains(approver)){
                    throw new Exception("Approver does not have rights");
                } else {
                    adminMap.put(group, user);
                    return "SUCCESS";
                }
            }
        }
    }

}
