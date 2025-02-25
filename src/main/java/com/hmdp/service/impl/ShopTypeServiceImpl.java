package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getTypeList() {
        String typeName = RedisConstants.CACHE_TYPE_KEY;

        Long typeListSize = stringRedisTemplate.opsForList().size(typeName);

        if (typeListSize != null && typeListSize != 0){
            List<String> typeJsonList = stringRedisTemplate.opsForList().range(typeName, 0, typeListSize - 1);
            List<ShopType> typeList = new ArrayList<>();
            assert typeJsonList != null;
            for (String typeJson : typeJsonList) {
                typeList.add(JSONUtil.toBean(typeJson, ShopType.class));
            }
            return Result.ok(typeList);
        }

        List<ShopType> typeList = query().orderByAsc("sort").list();
        if (typeList==null){
            return Result.fail("发生错误");
        }
        for (ShopType shopType : typeList) {
            stringRedisTemplate.opsForList().rightPush(typeName,JSONUtil.toJsonStr(shopType));
        }
        return Result.ok(typeList);
    }
}
