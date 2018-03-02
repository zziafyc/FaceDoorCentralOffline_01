package com.example.facedoor.util;

import com.iflytek.cloud.SpeechError;

public class ErrorDesc {
	public static String getDesc(SpeechError e){
		String desc = e.getErrorDescription();
		switch (e.getErrorCode()) {
		case 11700:
			desc = "未检测到人脸"; 
			break;
		case 11701:
			desc = "向左"; 
			break;
		case 11702:
			desc = "向右"; 
			break;
		case 11703:
			desc = "顺时针旋转 "; 
			break;
		case 11704:
			desc = "逆时针旋转"; 
			break;
		case 11705:
			desc = "尺寸错误"; 
			break;
		case 11706:
			desc = "光照异常"; 
			break;
		case 11707:
			desc = "人脸被遮挡 "; 
			break;
		case 11708:
			desc = "非法模型数据 "; 
			break;
		case 11709:
			desc = "输入数据类型非法"; 
			break;
		case 11710:
			desc = "输入的数据不完整 "; 
			break;
		case 11711:
			desc = "输入的数据过多 "; 
			break;
		case 11600:
			desc = "内核异常"; 
			break;
		case 11601:
			desc = "rgn超过最大支持次数9"; 
			break;
		case 11602:
			desc = "音频波形幅度太大，超出系统范围，发生截幅"; 
			break;
		case 11603:
			desc = "太多噪音"; 
			break;
		case 11604:
			desc = "声音太小"; 
			break;
		case 11605:
			desc = "没检测到音频"; 
			break;
		case 11606:
			desc = "音频太短 "; 
			break;
		case 11607:
			desc = "音频内容与给定文本不一致"; 
			break;
		case 11608:
			desc = "音频长达不到自由说的要求"; 
			break;
		case 11610:
			desc = "声纹模型数据在hbase中找不到"; 
			break;
		case 10116:
			desc = "模型不存在";
			break;
		case 10141:
			desc = "你输入的组没有添加成员";
			break;
		case 10142:
			desc = "没有此用户";
			break;
		case 10143:
			desc = "你输入的组尚未创建";
			break;
		case 10144:
			desc = "创建的组或者组中的成员超过限制";
			break;
		default:
			break;
		}
		return desc;
	}
}
