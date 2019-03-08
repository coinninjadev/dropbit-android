package com.coinninja.coinkeeper.service.client.model;

public class CNGlobalMessage {
    String id;
    long created_at;
    long updated_at;
    String subject;
    String body;
    String level;
    Metadata metadata;
    String platform;
    int priority;
    long published_at;
    String url;
    String version;

    public String getId() {
        return id;
    }

    public long getCreated_at() {
        return created_at * 1000;
    }

    public long getUpdated_at() {
        return updated_at * 1000;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getLevel() {
        return level;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public String getPlatform() {
        return platform;
    }

    public int getPriority() {
        return priority;
    }

    public long getPublished_at() {
        return published_at * 1000;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }


    public class Metadata {
        long display_at;
        long display_ttl;

        public long getDisplay_at() {
            return display_at;
        }

        public long getDisplay_ttl() {
            return display_ttl;
        }
    }
}

















