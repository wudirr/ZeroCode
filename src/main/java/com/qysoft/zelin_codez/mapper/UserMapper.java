package com.qysoft.zelin_codez.mapper;

import com.mybatisflex.core.BaseMapper;
import com.qysoft.zelin_codez.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 映射层。
 *
 * @author wudi
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
