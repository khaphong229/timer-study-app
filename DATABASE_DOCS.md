# 🗄️ Timer Study App - Database Documentation

## 📖 Tổng Quan

Database được xây dựng bằng **Room Database** với 10 Entity classes, hỗ trợ đầy đủ các tính năng của ứng dụng Timer Study. Database được thiết kế để quản lý người dùng, phiên học tập, nhiệm vụ, mục tiêu và thống kê.

---

## 🏗️ Kiến Trúc Database

```
┌─────────────────────────────────────────────────────────────┐
│                    ROOM DATABASE                            │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                ENTITY LAYER                          │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │    │
│  │  │   User      │  │  Session    │  │    Task     │  │    │
│  │  │  Entity     │  │  Entity     │  │  Entity     │  │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │    │
│  │  │    Goal     │  │   Setting   │  │ Statistics  │  │    │
│  │  │  Entity     │  │  Entity     │  │  Entity     │  │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              TYPE CONVERTERS                        │    │
│  │  ┌─────────────────────────────────────────────┐    │    │
│  │  │           DateConverter                     │    │    │
│  │  │  Date ↔ Long (timestamp)                   │    │    │
│  │  └─────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 Entity Classes Chi Tiết

### 1️⃣ **UserEntity** - Quản Lý Người Dùng

**Bảng**: `users`

| Cột | Kiểu | Mô tả | Constraints |
|-----|------|-------|-------------|
| `user_id` | `INTEGER` | Primary Key, Auto Generate | `@PrimaryKey` |
| `email` | `TEXT` | Email người dùng | `@Index` |
| `display_name` | `TEXT` | Tên hiển thị | - |
| `profile_picture_url` | `TEXT` | URL ảnh đại diện | - |
| `created_at` | `INTEGER` | Thời gian tạo (timestamp) | `@TypeConverters` |
| `last_login` | `INTEGER` | Lần đăng nhập cuối | `@TypeConverters` |
| `is_anonymous` | `INTEGER` | Có phải user ẩn danh | `DEFAULT 1` |

**Chức năng hỗ trợ**:
- ✅ Tạo user mới (anonymous)
- ✅ Cập nhật thông tin profile
- ✅ Theo dõi lần đăng nhập cuối
- ✅ Quản lý user anonymous

---

### 2️⃣ **SessionEntity** - Phiên Học Tập

**Bảng**: `sessions`

| Cột | Kiểu | Mô tả | Constraints |
|-----|------|-------|-------------|
| `session_id` | `INTEGER` | Primary Key, Auto Generate | `@PrimaryKey` |
| `user_id` | `INTEGER` | Foreign Key → users.user_id | `@ForeignKey CASCADE` |
| `session_date` | `INTEGER` | Ngày session (timestamp) | `@TypeConverters` |
| `start_time` | `INTEGER` | Thời gian bắt đầu | `@TypeConverters` |
| `end_time` | `INTEGER` | Thời gian kết thúc | `@TypeConverters` |
| `duration_minutes` | `INTEGER` | Thời lượng dự kiến (phút) | - |
| `actual_duration_minutes` | `INTEGER` | Thời lượng thực tế | `NULLABLE` |
| `session_type` | `TEXT` | Loại session | `@Index` |
| `status` | `TEXT` | Trạng thái session | `DEFAULT "IN_PROGRESS"` |
| `focus_session_count` | `INTEGER` | Số phiên focus | `DEFAULT 0` |
| `is_completed` | `INTEGER` | Đã hoàn thành | `DEFAULT 0` |
| `pause_count` | `INTEGER` | Số lần tạm dừng | `DEFAULT 0` |
| `total_pause_duration` | `INTEGER` | Tổng thời gian tạm dừng | `DEFAULT 0` |
| `created_at` | `INTEGER` | Thời gian tạo | `@TypeConverters` |
| `updated_at` | `INTEGER` | Thời gian cập nhật | `@TypeConverters` |

**Constants**:
```java
// Session Types
TYPE_FOCUS_SESSION = "FOCUS_SESSION"
TYPE_SHORT_BREAK = "SHORT_BREAK"  
TYPE_LONG_BREAK = "LONG_BREAK"

// Session Status
STATUS_IN_PROGRESS = "IN_PROGRESS"
STATUS_COMPLETED = "COMPLETED"
STATUS_PAUSED = "PAUSED"
STATUS_CANCELLED = "CANCELLED"
```

**Chức năng hỗ trợ**:
- ✅ Tạo session mới (focus/break)
- ✅ Theo dõi thời gian thực tế
- ✅ Quản lý trạng thái session
- ✅ Đếm số lần tạm dừng
- ✅ Tính toán thời gian hiệu quả

---

### 3️⃣ **SessionPauseEntity** - Tạm Dừng Session

**Bảng**: `session_pauses`

| Cột | Kiểu | Mô tả | Constraints |
|-----|------|-------|-------------|
| `pause_id` | `INTEGER` | Primary Key, Auto Generate | `@PrimaryKey` |
| `session_id` | `INTEGER` | Foreign Key → sessions.session_id | `@ForeignKey CASCADE` |
| `pause_start` | `INTEGER` | Thời gian bắt đầu tạm dừng | `@TypeConverters` |
| `pause_end` | `INTEGER` | Thời gian kết thúc tạm dừng | `@TypeConverters` |
| `pause_duration` | `INTEGER` | Thời lượng tạm dừng (phút) | `NULLABLE` |

**Chức năng hỗ trợ**:
- ✅ Ghi lại từng lần tạm dừng
- ✅ Tính toán thời gian tạm dừng
- ✅ Liên kết với session chính

---

### 4️⃣ **TaskEntity** - Quản Lý Nhiệm Vụ

**Bảng**: `tasks`

| Cột | Kiểu | Mô tả | Constraints |
|-----|------|-------|-------------|
| `task_id` | `INTEGER` | Primary Key, Auto Generate | `@PrimaryKey` |
| `user_id` | `INTEGER` | Foreign Key → users.user_id | `@ForeignKey CASCADE` |
| `title` | `TEXT` | Tiêu đề nhiệm vụ | - |
| `description` | `TEXT` | Mô tả chi tiết | - |
| `priority` | `TEXT` | Độ ưu tiên | `DEFAULT "MEDIUM"` |
| `task_date` | `INTEGER` | Ngày thực hiện | `@TypeConverters` |
| `is_completed` | `INTEGER` | Đã hoàn thành | `DEFAULT 0` |
| `completed_at` | `INTEGER` | Thời gian hoàn thành | `@TypeConverters` |
| `total_time_spent` | `INTEGER` | Tổng thời gian đã dành | `DEFAULT 0` |
| `estimated_sessions` | `INTEGER` | Số session ước tính | `DEFAULT 1` |
| `actual_sessions` | `INTEGER` | Số session thực tế | `DEFAULT 0` |
| `order_index` | `INTEGER` | Thứ tự sắp xếp | `DEFAULT 0` |
| `created_at` | `INTEGER` | Thời gian tạo | `@TypeConverters` |
| `updated_at` | `INTEGER` | Thời gian cập nhật | `@TypeConverters` |

**Constants**:
```java
PRIORITY_HIGH = "HIGH"
PRIORITY_MEDIUM = "MEDIUM"
PRIORITY_LOW = "LOW"
```

**Chức năng hỗ trợ**:
- ✅ Tạo và quản lý danh sách nhiệm vụ
- ✅ Phân loại theo độ ưu tiên
- ✅ Theo dõi tiến độ hoàn thành
- ✅ Tính toán thời gian đã dành
- ✅ Sắp xếp theo thứ tự

---

### 5️⃣ **TaskSessionEntity** - Liên Kết Task-Session

**Bảng**: `task_sessions`

| Cột | Kiểu | Mô tả | Constraints |
|-----|------|-------|-------------|
| `task_session_id` | `INTEGER` | Primary Key, Auto Generate | `@PrimaryKey` |
| `task_id` | `INTEGER` | Foreign Key → tasks.task_id | `@ForeignKey CASCADE` |
| `session_id` | `INTEGER` | Foreign Key → sessions.session_id | `@ForeignKey CASCADE` |
| `time_spent` | `INTEGER` | Thời gian dành cho task trong session | - |
| `notes` | `TEXT` | Ghi chú cho session-task | - |
| `created_at` | `INTEGER` | Thời gian tạo | `@TypeConverters` |

**Chức năng hỗ trợ**:
- ✅ Liên kết nhiều task với một session
- ✅ Theo dõi thời gian dành cho từng task
- ✅ Ghi chú cho từng liên kết
- ✅ Quan hệ many-to-many

---

### 6️⃣ **GoalEntity** - Mục Tiêu Hàng Ngày

**Bảng**: `goals`

| Cột | Kiểu | Mô tả | Constraints |
|-----|------|-------|-------------|
| `goal_id` | `INTEGER` | Primary Key, Auto Generate | `@PrimaryKey` |
| `user_id` | `INTEGER` | Foreign Key → users.user_id | `@ForeignKey CASCADE` |
| `goal_date` | `INTEGER` | Ngày mục tiêu | `@TypeConverters` |
| `target_sessions` | `INTEGER` | Số session mục tiêu | - |
| `completed_sessions` | `INTEGER` | Số session đã hoàn thành | `DEFAULT 0` |
| `completion_percentage` | `INTEGER` | Phần trăm hoàn thành | `DEFAULT 0` |
| `is_achieved` | `INTEGER` | Đã đạt mục tiêu | `DEFAULT 0` |
| `achieved_at` | `INTEGER` | Thời gian đạt mục tiêu | `@TypeConverters` |
| `created_at` | `INTEGER` | Thời gian tạo | `@TypeConverters` |
| `updated_at` | `INTEGER` | Thời gian cập nhật | `@TypeConverters` |

**Chức năng hỗ trợ**:
- ✅ Tạo mục tiêu hàng ngày
- ✅ Theo dõi tiến độ hoàn thành
- ✅ Tính toán phần trăm hoàn thành
- ✅ Đánh dấu đã đạt mục tiêu

---

### 7️⃣ **UserSettingEntity** - Cài Đặt Cá Nhân

**Bảng**: `user_settings`

| Cột | Kiểu | Mô tả | Constraints |
|-----|------|-------|-------------|
| `setting_id` | `INTEGER` | Primary Key, Auto Generate | `@PrimaryKey` |
| `user_id` | `INTEGER` | Foreign Key → users.user_id | `@ForeignKey CASCADE` |
| `setting_key` | `TEXT` | Khóa cài đặt | `UNIQUE(user_id, setting_key)` |
| `setting_value` | `TEXT` | Giá trị cài đặt | - |
| `data_type` | `TEXT` | Kiểu dữ liệu | `DEFAULT "STRING"` |
| `created_at` | `INTEGER` | Thời gian tạo | `@TypeConverters` |
| `updated_at` | `INTEGER` | Thời gian cập nhật | `@TypeConverters` |

**Constants**:
```java
TYPE_STRING = "STRING"
TYPE_INTEGER = "INTEGER"
TYPE_BOOLEAN = "BOOLEAN"
TYPE_JSON = "JSON"
```

**Chức năng hỗ trợ**:
- ✅ Lưu trữ cài đặt cá nhân
- ✅ Hỗ trợ nhiều kiểu dữ liệu
- ✅ Cập nhật cài đặt động
- ✅ Mỗi user có bộ cài đặt riêng

---

### 8️⃣ **DefaultSettingEntity** - Cài Đặt Mặc Định

**Bảng**: `default_settings`

| Cột | Kiểu | Mô tả | Constraints |
|-----|------|-------|-------------|
| `default_setting_id` | `INTEGER` | Primary Key, Auto Generate | `@PrimaryKey` |
| `setting_key` | `TEXT` | Khóa cài đặt | `UNIQUE` |
| `default_value` | `TEXT` | Giá trị mặc định | - |
| `data_type` | `TEXT` | Kiểu dữ liệu | - |
| `category` | `TEXT` | Danh mục cài đặt | `@Index` |
| `description` | `TEXT` | Mô tả cài đặt | - |
| `is_configurable` | `INTEGER` | Có thể cấu hình | `DEFAULT 1` |

**Constants**:
```java
CATEGORY_TIMER = "TIMER"
CATEGORY_NOTIFICATION = "NOTIFICATION"
CATEGORY_APPEARANCE = "APPEARANCE"
CATEGORY_DATA = "DATA"
```

**Chức năng hỗ trợ**:
- ✅ Cài đặt mặc định cho app
- ✅ Phân loại theo danh mục
- ✅ Kiểm soát cài đặt có thể thay đổi

---

### 9️⃣ **StatisticsCacheEntity** - Cache Thống Kê

**Bảng**: `statistics_cache`

| Cột | Kiểu | Mô tả | Constraints |
|-----|------|-------|-------------|
| `cache_id` | `INTEGER` | Primary Key, Auto Generate | `@PrimaryKey` |
| `user_id` | `INTEGER` | Foreign Key → users.user_id | `@ForeignKey CASCADE` |
| `cache_date` | `INTEGER` | Ngày cache | `@TypeConverters` |
| `cache_type` | `TEXT` | Loại cache | `UNIQUE(user_id, cache_date, cache_type)` |
| `total_sessions` | `INTEGER` | Tổng số session | `DEFAULT 0` |
| `total_focus_time` | `INTEGER` | Tổng thời gian focus | `DEFAULT 0` |
| `total_break_time` | `INTEGER` | Tổng thời gian nghỉ | `DEFAULT 0` |
| `completed_tasks` | `INTEGER` | Số task hoàn thành | `DEFAULT 0` |
| `goal_achieved` | `INTEGER` | Đã đạt mục tiêu | `DEFAULT 0` |
| `current_streak` | `INTEGER` | Chuỗi hiện tại | `DEFAULT 0` |
| `best_streak` | `INTEGER` | Chuỗi tốt nhất | `DEFAULT 0` |
| `cached_at` | `INTEGER` | Thời gian cache | `@TypeConverters` |

**Constants**:
```java
TYPE_DAILY = "DAILY"
TYPE_MONTHLY = "MONTHLY"
TYPE_YEARLY = "YEARLY"
```

**Chức năng hỗ trợ**:
- ✅ Cache thống kê để tối ưu hiệu năng
- ✅ Hỗ trợ nhiều loại thống kê
- ✅ Theo dõi chuỗi ngày học tập
- ✅ Thống kê tổng quan

---

### 🔟 **StreakRecordEntity** - Theo Dõi Chuỗi

**Bảng**: `streak_records`

| Cột | Kiểu | Mô tả | Constraints |
|-----|------|-------|-------------|
| `streak_id` | `INTEGER` | Primary Key, Auto Generate | `@PrimaryKey` |
| `user_id` | `INTEGER` | Foreign Key → users.user_id | `@ForeignKey CASCADE` |
| `streak_date` | `INTEGER` | Ngày trong chuỗi | `@TypeConverters` |
| `has_activity` | `INTEGER` | Có hoạt động | `DEFAULT 0` |
| `session_count` | `INTEGER` | Số session trong ngày | `DEFAULT 0` |
| `focus_time` | `INTEGER` | Thời gian focus (phút) | `DEFAULT 0` |

**Chức năng hỗ trợ**:
- ✅ Theo dõi hoạt động hàng ngày
- ✅ Tính toán chuỗi ngày học tập
- ✅ Thống kê session theo ngày

---

## 🔧 Type Converters

### **DateConverter**

**Chức năng**: Chuyển đổi giữa `Date` object và `Long` timestamp

```java
@TypeConverter
public static Date fromTimestamp(Long value) {
    return value == null ? null : new Date(value);
}

@TypeConverter
public static Long dateToTimestamp(Date date) {
    return date == null ? null : date.getTime();
}
```

**Sử dụng**: Tự động áp dụng cho tất cả các trường `Date` trong Entity classes

---

## 🔗 Relationships & Foreign Keys

### **User → Sessions**
```
users.user_id → sessions.user_id (CASCADE DELETE)
```

### **User → Tasks**
```
users.user_id → tasks.user_id (CASCADE DELETE)
```

### **User → Goals**
```
users.user_id → goals.user_id (CASCADE DELETE)
```

### **User → Settings**
```
users.user_id → user_settings.user_id (CASCADE DELETE)
```

### **Session → Pauses**
```
sessions.session_id → session_pauses.session_id (CASCADE DELETE)
```

### **Task ↔ Session (Many-to-Many)**
```
tasks.task_id → task_sessions.task_id (CASCADE DELETE)
sessions.session_id → task_sessions.session_id (CASCADE DELETE)
```

---

## 📈 Indexing Strategy

### **Performance Indexes**

| Bảng | Cột | Loại | Mục đích |
|------|-----|------|----------|
| `users` | `email` | Single | Tìm kiếm user |
| `users` | `created_at` | Single | Sắp xếp theo thời gian tạo |
| `sessions` | `user_id` | Single | Lọc session theo user |
| `sessions` | `session_date` | Single | Lọc theo ngày |
| `sessions` | `(user_id, session_date)` | Composite | Lọc session user theo ngày |
| `sessions` | `session_type` | Single | Lọc theo loại session |
| `sessions` | `status` | Single | Lọc theo trạng thái |
| `tasks` | `user_id` | Single | Lọc task theo user |
| `tasks` | `task_date` | Single | Lọc theo ngày |
| `tasks` | `(user_id, task_date)` | Composite | Lọc task user theo ngày |
| `tasks` | `priority` | Single | Sắp xếp theo độ ưu tiên |
| `tasks` | `is_completed` | Single | Lọc task hoàn thành |
| `goals` | `user_id` | Single | Lọc goal theo user |
| `goals` | `goal_date` | Single | Lọc theo ngày |
| `goals` | `(user_id, goal_date)` | Unique | Một goal mỗi user mỗi ngày |

---

## 🚀 Database Features

### ✅ **Đã Hỗ Trợ**

1. **Entity Management**
   - 10 Entity classes hoàn chỉnh
   - Primary keys với auto generation
   - Foreign key relationships
   - CASCADE delete cho data integrity

2. **Data Types**
   - Type converters cho Date objects
   - Support cho nullable fields
   - Default values cho các trường quan trọng

3. **Performance**
   - Comprehensive indexing
   - Composite indexes cho queries phức tạp
   - Statistics caching

4. **Data Integrity**
   - Foreign key constraints
   - Unique constraints
   - Default values
   - NOT NULL constraints

5. **Flexibility**
   - User settings với multiple data types
   - Configurable default settings
   - Extensible entity structure

---

## 📋 Các Hàm Hỗ Trợ

### **Entity Classes Functions**

Mỗi Entity class đều có đầy đủ:

```java
// Constructors
public EntityName() { /* Default constructor */ }

// Getters
public DataType getFieldName() { return fieldName; }

// Setters  
public void setFieldName(DataType fieldName) { this.fieldName = fieldName; }
```

### **Type Converter Functions**

```java
// Convert Long timestamp to Date
public static Date fromTimestamp(Long value)

// Convert Date to Long timestamp
public static Long dateToTimestamp(Date date)
```

---

## 🔄 Database Operations

### **CRUD Operations** (Cần implement DAO)

```java
// Create
@Insert
void insertEntity(EntityName entity);

// Read
@Query("SELECT * FROM table_name WHERE condition")
List<EntityName> getEntities();

// Update
@Update
void updateEntity(EntityName entity);

// Delete
@Delete
void deleteEntity(EntityName entity);
```

### **Advanced Queries** (Cần implement DAO)

```java
// Complex joins
@Query("SELECT * FROM sessions s JOIN users u ON s.user_id = u.user_id")
List<SessionWithUser> getSessionsWithUsers();

// Aggregations
@Query("SELECT COUNT(*) FROM sessions WHERE user_id = :userId")
int getSessionCount(int userId);

// Date range queries
@Query("SELECT * FROM sessions WHERE session_date BETWEEN :start AND :end")
List<SessionEntity> getSessionsInRange(long start, long end);
```

---

## 🎯 Next Steps

### **Cần Implement**

1. **DAO Classes**
   - UserDao
   - SessionDao
   - TaskDao
   - GoalDao
   - SettingDao
   - StatisticsDao

2. **Database Class**
   - AppDatabase với @Database annotation
   - Migration strategies
   - Database versioning

3. **Repository Layer**
   - Database operations
   - Data synchronization
   - Error handling

4. **Testing**
   - Unit tests cho Entity classes
   - Integration tests cho database
   - Migration tests

---

## 📚 Tài Liệu Tham Khảo

- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Entity Relationships](https://developer.android.com/training/data-storage/room/relationships)
- [Type Converters](https://developer.android.com/training/data-storage/room/referencing-data)
- [Database Migrations](https://developer.android.com/training/data-storage/room/migrating-db-schema)

---

## 👨‍💻 Author

**Database Schema**: Timer Study App  
**Version**: 1.0  
**Last Updated**: 2025  
**License**: MIT

---

