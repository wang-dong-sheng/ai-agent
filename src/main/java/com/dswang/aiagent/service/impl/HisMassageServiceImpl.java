//package com.dswang.aiagent.service.impl;
//
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.dswang.aiagent.domain.HisMassage;
//import com.dswang.aiagent.mapper.HisMassageMapper;
//import com.dswang.aiagent.service.HisMassageService;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
///**
//* @author Administrator
//* @description 针对表【his_massage(历史对话信息)】的数据库操作Service实现
//* @createDate 2026-01-07 10:25:28
//*/
//@Service
//public class HisMassageServiceImpl extends ServiceImpl<HisMassageMapper, HisMassage>
//    implements HisMassageService {
//
//    @Override
//    public List<HisMassage> getLatestMessages(String conversationId, int limit) {
//        return this.baseMapper.getLatestMessages(conversationId, limit);
//    }
//
//    @Override
//    public void clearByConversationId(String conversationId) {
//        this.baseMapper.clearByConversationId(conversationId);
//    }
//}