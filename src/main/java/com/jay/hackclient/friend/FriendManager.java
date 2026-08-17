package com.jay.hackclient.friend;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FriendManager {

    private final Set<String> friends = new HashSet<>();

    public void add(String name) {
        if (name != null && !name.isBlank()) {
            friends.add(name.toLowerCase(Locale.ROOT));
        }
    }

    public void remove(String name) {
        if (name != null) friends.remove(name.toLowerCase(Locale.ROOT));
    }

    public boolean isFriend(String name) {
        return name != null && friends.contains(name.toLowerCase(Locale.ROOT));
    }

    public Set<String> getFriends() {
        return Collections.unmodifiableSet(friends);
    }

    public void clear() {
        friends.clear();
    }

    public void setAll(Set<String> names) {
        friends.clear();
        for (String n : names) add(n);
    }
}
