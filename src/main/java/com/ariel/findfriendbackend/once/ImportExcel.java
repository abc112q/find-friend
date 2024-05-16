package com.ariel.findfriendbackend.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * 导入 Excel
 * @author Ariel
 */
public class ImportExcel {

    /**
     * 读取数据
     */
    public static void main(String[] args) {
        // todo 记得改为自己的测试文件
        String fileName = "";
//        readByListener(fileName);
        synchronousRead(fileName);
    }

    /**
     * 监听器读取
     *
     * @param fileName
     */
    public static void readByListener(String fileName) {
        EasyExcel.read(fileName, UserTableInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读
     *
     * @param fileName
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<UserTableInfo> totalDataList =
                EasyExcel.read(fileName).head(UserTableInfo.class).sheet().doReadSync();
        for (UserTableInfo TableUserInfo : totalDataList) {
            System.out.println(TableUserInfo);
        }
    }

}