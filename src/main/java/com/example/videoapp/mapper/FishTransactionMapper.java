package com.example.videoapp.mapper;

import com.example.videoapp.entity.FishTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FishTransactionMapper {
    int insert(FishTransaction transaction);
    void deleteByVideoId(Long videoId);
}