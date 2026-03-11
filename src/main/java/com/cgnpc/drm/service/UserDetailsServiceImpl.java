package com.cgnpc.drm.service;

import com.cgnpc.drm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户详情服务实现类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 这里的 username 可能是邮箱或手机号
        User user = userService.findByEmail(username);
        if (user == null) {
            user = userService.findByPhone(username);
        }

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 这里可以添加用户权限信息
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.getStatus() == 0)
                .credentialsExpired(false)
                .disabled(user.getStatus() == 0)
                .build();
    }
}
