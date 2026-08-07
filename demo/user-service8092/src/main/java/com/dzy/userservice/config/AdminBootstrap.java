package com.dzy.userservice.config;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.User;
import com.dzy.userservice.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;


@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";
    private static final String ADMIN_PHONE = "13800000000";

    private final UserMapper userMapper;

    public AdminBootstrap(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            User admin = userMapper.selectByUsername(ADMIN_USER);
            if (admin == null) {
                User user = new User();
                user.setUsername(ADMIN_USER);
                user.setPhone(ADMIN_PHONE);
                user.setRole(Constants.ROLE_ADMIN);
                user.setStatus(1);
                user.setPasswordHash(BCrypt.hashpw(ADMIN_PASS, BCrypt.gensalt()));
                userMapper.addUser(user);
                log.info("已创建默认管理员账号 {} / {}", ADMIN_USER, ADMIN_PASS);
                return;
            }
            String hash = admin.getPasswordHash();
            boolean ok = false;
            try {
                ok = hash != null && !hash.isBlank() && BCrypt.checkpw(ADMIN_PASS, hash);
            } catch (Exception e) {
                log.warn("管理员密码哈希无效，将重置: {}", e.getMessage());
            }
            if (!ok) {
                userMapper.updatePassword(admin.getId(), BCrypt.hashpw(ADMIN_PASS, BCrypt.gensalt()));
                log.info("已重置管理员密码为 {} / {}", ADMIN_USER, ADMIN_PASS);
            }
            if (admin.getStatus() != null && admin.getStatus() == 0) {
                userMapper.updateUserStatus(admin.getId(), 1);
                log.info("已启用管理员账号");
            }
        } catch (Exception e) {
            log.error("初始化管理员账号失败（请检查 user_db 是否已建表）: {}", e.getMessage());
        }
    }
}
