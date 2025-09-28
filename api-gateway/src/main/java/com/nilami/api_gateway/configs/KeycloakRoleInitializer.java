package com.nilami.api_gateway.configs;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nilami.api_gateway.models.Roles;

@Configuration
public class KeycloakRoleInitializer {

    private final Keycloak keycloak;

    private KeycloakClientProperties keycloakClientProperties;

    public KeycloakRoleInitializer(Keycloak keycloak,KeycloakClientProperties keycloakClientProperties ) {
        this.keycloak = keycloak;
        this.keycloakClientProperties=keycloakClientProperties;
    }

    @Bean
    CommandLineRunner initRoles() {
        return args -> {
            RealmResource realmResource = keycloak.realm(keycloakClientProperties.getKeyCloakRealm());

            for (Roles roleEnum : Roles.values()) {
                String roleName = roleEnum.name(); // CUSTOMER, SELLER, ADMIN

                boolean roleExists = realmResource.roles().list()
                        .stream()
                        .anyMatch(r -> r.getName().equals(roleName));

                if (!roleExists) {
                    RoleRepresentation role = new RoleRepresentation();
                    role.setName(roleName);
                    role.setDescription("rolezz: " + roleName);

                    realmResource.roles().create(role);
                    System.out.println("created role: " + roleName);
                } else {
                    System.out.println("role already exists: " + roleName);
                }
            }
        };
    }
}
