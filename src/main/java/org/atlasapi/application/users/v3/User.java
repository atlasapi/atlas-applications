package org.atlasapi.application.users.v3;

import java.util.Set;

import org.atlasapi.application.v3.Application;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.social.model.UserRef;

public class User {

    private final Long id;
    private final UserRef userRef;
    private final String screenName;
    private final String fullName;
    private final String company;
    private final String email;
    private final String website;
    private final String profileImage;
    private final Role role;
    private boolean profileComplete;
    private final Set<String> applicationSlugs;
    private final Set<Publisher> publishers;

    private User(Long id, UserRef userRef, String screenName, String fullName, String company,
            String email, String website, String profileImage, Role role,
            Set<String> applicationSlugs, Set<Publisher> publishers, boolean profileComplete) {
        this.id = id;
        this.userRef = userRef;
        this.screenName = screenName;
        this.fullName = fullName;
        this.company = company;
        this.email = email;
        this.website = website;
        this.profileImage = profileImage;
        this.role = role;
        this.applicationSlugs = ImmutableSet.copyOf(applicationSlugs);
        this.publishers = ImmutableSet.copyOf(publishers);
        this.profileComplete = profileComplete;
    }

    public Long getId() {
        return this.id;
    }

    public UserRef getUserRef() {
        return this.userRef;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCompany() {
        return company;
    }

    public String getEmail() {
        return email;
    }

    public String getWebsite() {
        return website;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public Role getRole() {
        return this.role;
    }

    public Set<String> getApplicationSlugs() {
        return applicationSlugs;
    }

    public Set<Publisher> getSources() {
        return this.publishers;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public boolean is(Role role) {
        return this.role == role;
    }

    public User copyWithAddedApplication(Application application) {
        return this.copy().withApplicationSlugs(
                ImmutableSet.<String>builder().add(application.getSlug()).addAll(this.getApplicationSlugs()).build())
                .build();
     }

    public boolean manages(Application application) {
        return manages(application.getSlug());
    }

    public boolean manages(String applicationSlug) {
        return applicationSlugs.contains(applicationSlug);
    }

    public boolean manages(Maybe<Publisher> possibleSource) {
        return possibleSource.hasValue() && publishers.contains(possibleSource.requireValue());
    }

    public Builder copy() {
        return new Builder()
                    .withId(this.getId())
                    .withUserRef(this.getUserRef())
                    .withScreenName(this.getScreenName())
                    .withFullName(this.getFullName())
                    .withCompany(this.getCompany())
                    .withEmail(this.getEmail())
                    .withWebsite(this.getWebsite())
                    .withProfileImage(this.getProfileImage())
                    .withApplicationSlugs(this.getApplicationSlugs())
                    .withSources(this.getSources())
                    .withRole(this.getRole())
                    .withProfileComplete(this.isProfileComplete());
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private UserRef userRef;
        private String screenName;
        private String fullName;
        private String company;
        private String email;
        private String website;
        private String profileImage;
        private Role role = Role.REGULAR;
        private boolean profileComplete;
        private Set<String> applicationSlugs = ImmutableSet.of();
        private Set<Publisher> publishers = ImmutableSet.of();
        
        public Builder withId(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder withUserRef(UserRef userRef) {
            this.userRef = userRef;
            return this;
        }
        
        public Builder withScreenName(String screenName) {
            this.screenName = screenName;
            return this;
        }
        
        public Builder withFullName(String fullName) {
            this.fullName = fullName;
            return this;
        }
        
        public Builder withCompany(String company) {
            this.company = company;
            return this;
        }
        
        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }
        
        public Builder withWebsite(String website) {
            this.website = website;
            return this;
        }
        
        public Builder withProfileImage(String profileImage) {
            this.profileImage = profileImage;
            return this;
        }
        
        public Builder withRole(Role role) {
            this.role = role;
            return this;
        }
        
        public Set<String> getApplicationSlugs() {
            return applicationSlugs;
        }
        
        public Set<Publisher> getPublishers() {
            return publishers;
        }
        
        public Builder withApplicationSlugs(Set<String> applicationSlugs) {
            this.applicationSlugs = applicationSlugs;
            return this;
        }
        
        public Builder withSources(Set<Publisher> publishers) {
            this.publishers = publishers;
            return this;
        }

        public Builder withProfileComplete(boolean profileComplete) {
            this.profileComplete = profileComplete;
            return this;
        }
        
        public User build() {
            return new User(id, userRef, screenName, fullName, company, email, website, profileImage, 
                    role, applicationSlugs, publishers, profileComplete);
        }
    }
    
}