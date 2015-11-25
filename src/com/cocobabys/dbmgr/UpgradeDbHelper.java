package com.cocobabys.dbmgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UpgradeDbHelper{

    private static final String              ANDROID_DEFAULT_TABLE = "android_metadata";
    private SQLiteDatabase                   db;
    private List<String>                     oldTables;
    private SqliteHelper                     dbhelper;

    private Map<String, CreateTableListener> tableMap              = new HashMap<String, UpgradeDbHelper.CreateTableListener>();

    public UpgradeDbHelper(SQLiteDatabase db, SqliteHelper helper){
        this.db = db;
        this.dbhelper = helper;
        initMap();
    }

    // 凡是数据库有新增的表时，都必须在下面添加进去
    private void initMap(){
        tableMap.put(SqliteHelper.CHILDREN_INFO_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addChildTab(db);
            }
        });

        tableMap.put(SqliteHelper.COOKBOOK_INFO_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addCookbookTab(db);
            }
        });

        tableMap.put(SqliteHelper.EDUCATION_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addEduTab(db);
            }
        });

        tableMap.put(SqliteHelper.EXP_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addExpTab(db);
            }
        });

        tableMap.put(SqliteHelper.HOMEWORK_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addHomeworkTab(db);
            }
        });

        tableMap.put(SqliteHelper.MONTH_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.initMonthTab(db);
            }
        });

        tableMap.put(SqliteHelper.NEW_CHAT_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addNewChatTab(db);
            }
        });

        tableMap.put(SqliteHelper.NEWS_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addNewsTab(db);
            }
        });

        tableMap.put(SqliteHelper.PARENT_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addParentTab(db);
            }
        });

        tableMap.put(SqliteHelper.SCHEDULE_INFO_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addScheduleTab(db);
            }
        });

        tableMap.put(SqliteHelper.SCHOOL_INFO_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addSchoolInfoTab(db);
            }
        });

        tableMap.put(SqliteHelper.SWIPE_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addSwipeTab(db);
            }
        });

        tableMap.put(SqliteHelper.TEACHER_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addTeacherTab(db);
            }
        });

        tableMap.put(SqliteHelper.NATIVE_MEDIUM_URL_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addNativeMediumUrlTab(db);
            }
        });

        tableMap.put(SqliteHelper.RECEIPT_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addReceiptTab(db);
            }
        });

        tableMap.put(SqliteHelper.IM_GROUP_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addIMGroupTab(db);
            }
        });

        tableMap.put(SqliteHelper.GROUP_CHILDREN_INFO_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addGroupChildTab(db);
            }
        });

        tableMap.put(SqliteHelper.GROUP_PARENT_INFO_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addGroupParentTab(db);
            }
        });

        tableMap.put(SqliteHelper.RELATIONSHIP_INFO_TAB, new CreateTableListener(){
            @Override
            public void onCreateTable(){
                dbhelper.addRelationshipTab(db);
            }
        });
    }

    public void upgradeDb(){
        getOldTables();

        Set<Entry<String, CreateTableListener>> entrySet = tableMap.entrySet();
        Iterator<Entry<String, CreateTableListener>> iterator = entrySet.iterator();

        while(iterator.hasNext()){
            Entry<String, CreateTableListener> next = iterator.next();
            // 旧数据和新数据库都存在相同的表，那么将旧表改名，创建新表，再把旧表数据导入到新表，完成后删除
            // 改名后的旧表
            String tablename = next.getKey();
            CreateTableListener listener = next.getValue();
            // MONTH_TAB数据不需要更新，只需要新建
            if(oldTables.contains(tablename) && !SqliteHelper.MONTH_TAB.equals(tablename)){
                updateData(tablename, listener);
            } else{
                // 表只在新数据库中存在，旧数据库不存在，那么直接创建新表，注意如果有index也在这里创建
                listener.onCreateTable();
            }
        }

    }

    private void updateData(String tablename, CreateTableListener listener){
        String tmpName = tablename + "_tmp";
        boolean needRollback = false;
        try{
            // 修改旧表名为临时表名,如果临时表已经存在，则可能是上次未删除干净的垃圾数据，再次删除之
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", tmpName));
            String changeTableNameSql = String.format("ALTER TABLE %s RENAME TO %s", tablename, tmpName);
            Log.d("TABLE_NAME", "changeTableNameSql =" + changeTableNameSql);
            db.execSQL(changeTableNameSql);

            // 如果已经改名成功，那么如果下面的流程执行出错，就需要回滚了
            needRollback = true;

            // 获取旧表的全部列表，并格式化
            String columns = getAllColumns(tmpName);

            // 创建新表
            listener.onCreateTable();

            // 导入旧表数据到新表
            String addDataSql = String.format("insert into %s(%s) select * from %s", tablename, columns, tmpName);
            Log.d("TABLE_NAME", "addDataSql =" + addDataSql);
            db.execSQL(addDataSql);

            // 如果只是删除临时表出错，不需要回滚,但是会造成垃圾数据
            needRollback = false;
            // 删除旧表,注意是临时表名
            String deleteOldTableSql = String.format("DROP TABLE IF EXISTS %s", tmpName);
            db.execSQL(deleteOldTableSql);
        } catch(SQLException e){
            e.printStackTrace();
            if(needRollback){
                rollbackData(tablename, tmpName);
            }
        }

    }

    private void rollbackData(String tablename, String tmpName){
        try{
            String droptable = String.format("DROP TABLE IF EXISTS %s", tablename);
            db.execSQL(droptable);

            // 把临时表改回真实表名，相对于数据回滚
            String changeTableNameSql = String.format("ALTER TABLE %s RENAME TO %s", tmpName, tablename);
            Log.d("TABLE_NAME", "rollbak =" + changeTableNameSql);
            db.execSQL(changeTableNameSql);
        } catch(SQLException e){
            Log.d("TABLE_NAME", "rollbak fail");
            e.printStackTrace();
        }
    }

    private String getAllColumns(String tmpName){
        StringBuilder builder = new StringBuilder();
        String sql = String.format("PRAGMA table_info(%s)", tmpName);
        Cursor cursor = db.rawQuery(sql, null);

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String column = cursor.getString(1);
                Log.d("TABLE_NAME", "tablename =" + column);
                builder.append(column + ",");
                cursor.moveToNext();
            }

            // 去掉最后一个,
            builder.deleteCharAt(builder.length() - 1);
        }
        finally{
            if(cursor != null){
                cursor.close();
            }
        }
        return builder.toString();
    }

    private void getOldTables(){
        oldTables = new ArrayList<String>();
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String tablename = cursor.getString(0);
                Log.d("TABLE_NAME", "tablename =" + tablename);
                if(!ANDROID_DEFAULT_TABLE.equals(tablename)){
                    oldTables.add(tablename);
                }
                cursor.moveToNext();
            }
        }
        finally{
            if(cursor != null){
                cursor.close();
            }
        }
    }

    public interface CreateTableListener{
        public void onCreateTable();
    }
}
