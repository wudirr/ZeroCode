package com.qysoft.zelin_codez.user.mapper;

import com.mybatisflex.core.BaseMapper;
import com.qysoft.zelin_codez.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 映射层。
 *
 * @author wudi
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
