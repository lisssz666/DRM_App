package com.cgnpc.drm.repository;

import com.cgnpc.drm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 用户Repository接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     * @param phone 手机号
     * @return 用户信息
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查邮箱是否已存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否已存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
}