package com.university.deanery.security;

import com.university.deanery.model.User;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class AclService {

    private static final Map<String, Map<String, List<String>>> ACL_MATRIX = Map.of(
            "DEAN", Map.of(
                    "applications", List.of("READ", "UPDATE", "APPROVE"),
                    "students", List.of("READ", "UPDATE", "TRANSFER", "EXPEL"),
                    "orders", List.of("CREATE", "READ", "SIGN"),
                    "profile", List.of("READ", "UPDATE")
            ),
            "STUDENT", Map.of(
                    "profile", List.of("READ", "UPDATE"),
                    "applications", List.of("CREATE", "READ"),
                    "students", List.of("READ")
            ),
            "APPLICANT", Map.of(
                    "applications", List.of("CREATE", "READ"),
                    "profile", List.of("READ", "UPDATE")
            ),
            "TEACHER", Map.of(
                    "students", List.of("READ", "UPDATE"),
                    "profile", List.of("READ", "UPDATE")
            ),
            "ADMIN", Map.of(
                    "users", List.of("CREATE", "READ", "UPDATE", "DELETE"),
                    "profile", List.of("READ", "UPDATE")
            )
    );

    public boolean hasAccess(User user, String resource, String operation) {
        String role = user.getRole();
        if (!ACL_MATRIX.containsKey(role)) return false;
        Map<String, List<String>> roleRights = ACL_MATRIX.get(role);
        if (!roleRights.containsKey(resource)) return false;
        return roleRights.get(resource).contains(operation);
    }

    public void checkAccess(User user, String resource, String operation) {
        if (!hasAccess(user, resource, operation)) {
            throw new RuntimeException("Доступ запрещён: роль " + user.getRole() +
                    " не имеет права на " + operation + " для ресурса " + resource);
        }
    }
}