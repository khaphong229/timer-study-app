# 🚀 My Base App - Android MVP Project

## 📖 Giới Thiệu

Base Android project frontend hoàn chỉnh sử dụng:

- **Kiến trúc**: MVP (Model-View-Presenter)
- **Navigation**: Navigation Component với BottomNavigationView
- **UI**: Material Design 3
- **Binding**: ViewBinding (type-safe, hiệu năng cao)
- **Database**: Room Database với Entity classes và Type Converters
- **Ngôn ngữ**: Java

---

## 🏗️ Kiến Trúc MVP

```
┌─────────────────────────────────────────────┐
│                   VIEW LAYER                │
│  (Activity, Fragments, XML Layouts)         │
│  - Hiển thị UI                              │
│  - Nhận user input                          │
│  - Implement View interface                 │
│  - Passive, không chứa business logic       │
└──────────────────┬──────────────────────────┘
                   │ implements
                   │ contract interface
                   ▼
┌─────────────────────────────────────────────┐
│              PRESENTER LAYER                │
│  (HomePresenter, TimerPresenter, etc.)      │
│  - Business logic                           │
│  - Điều phối giữa View và Model             │
│  - Không biết về Android framework          │
│  - Testable                                 │
└──────────────────┬──────────────────────────┘
                   │ calls
                   │ methods
                   ▼
┌─────────────────────────────────────────────┐
│             REPOSITORY LAYER                │
│  (UserRepository, TimerRepository, etc.)    │
│  - Single source of truth                   │
│  - Data operations                          │
│  - API calls / Database                     │
│  - Return data via callbacks                │
└──────────────────┬──────────────────────────┘
                   │ returns
                   │ data
                   ▼
┌─────────────────────────────────────────────┐
│               MODEL LAYER                   │
│  (User, Timer, StudySession)                │
│  - Data classes                             │
│  - Plain Java objects                       │
│  - Business entities                        │
└─────────────────────────────────────────────┘
```

---

## 📁 Cấu Trúc Project

```
TimerStudy/
├── .gradle/                             # Gradle cache files
├── .idea/                               # Android Studio settings
├── app/
│   ├── build/                           # Build outputs
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/timerstudy/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── User.java              # Data models
│   │   │   │   │   │   ├── Timer.java             # Timer model
│   │   │   │   │   │   └── StudySession.java      # Study session model
│   │   │   │   │   │
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── UserRepository.java    # User data management
│   │   │   │   │   │   ├── TimerRepository.java   # Timer data management
│   │   │   │   │   │   └── StudyRepository.java   # Study session management
│   │   │   │   │   │
│   │   │   │   │   └── local/
│   │   │   │   │       ├── database/
│   │   │   │   │       │   ├── AppDatabase.java   # Room database
│   │   │   │   │       │   ├── dao/
│   │   │   │   │       │   │   ├── UserDao.java   # User data access
│   │   │   │   │       │   │   ├── SessionDao.java # Session data access
│   │   │   │   │       │   │   ├── TaskDao.java   # Task data access
│   │   │   │   │       │   │   └── GoalDao.java   # Goal data access
│   │   │   │   │       │   ├── entities/
│   │   │   │   │       │   │   ├── UserEntity.java
│   │   │   │   │       │   │   ├── SessionEntity.java
│   │   │   │   │       │   │   ├── TaskEntity.java
│   │   │   │   │       │   │   ├── GoalEntity.java
│   │   │   │   │       │   │   └── ... (10 entities total)
│   │   │   │   │       │   └── converters/
│   │   │   │   │       │       └── DateConverter.java # Date type converter
│   │   │   │   │       └── preferences/
│   │   │   │   │           └── AppPreferences.java # Shared preferences
│   │   │   │   │
│   │   │   │   ├── ui/
│   │   │   │   │   ├── activities/
│   │   │   │   │   │   ├── MainActivity.java       # Main NavHost container
│   │   │   │   │   │   └── SplashActivity.java     # Splash screen
│   │   │   │   │   │
│   │   │   │   │   ├── fragments/
│   │   │   │   │   │   ├── home/
│   │   │   │   │   │   │   ├── HomeFragment.java   # Timer dashboard
│   │   │   │   │   │   │   ├── HomePresenter.java  # Home presenter
│   │   │   │   │   │   │   └── HomeContract.java   # View-Presenter contract
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── timer/
│   │   │   │   │   │   │   ├── TimerFragment.java  # Timer functionality
│   │   │   │   │   │   │   ├── TimerPresenter.java # Timer presenter
│   │   │   │   │   │   │   └── TimerContract.java  # View-Presenter contract
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── statistics/
│   │   │   │   │   │   │   ├── StatisticsFragment.java # Study stats
│   │   │   │   │   │   │   ├── StatisticsPresenter.java
│   │   │   │   │   │   │   └── StatisticsContract.java
│   │   │   │   │   │   │
│   │   │   │   │   │   └── settings/
│   │   │   │   │   │       ├── SettingsFragment.java # App settings
│   │   │   │   │   │       ├── SettingsPresenter.java
│   │   │   │   │   │       └── SettingsContract.java
│   │   │   │   │   │
│   │   │   │   │   ├── adapters/
│   │   │   │   │   │   ├── StudySessionAdapter.java # RecyclerView adapter
│   │   │   │   │   │   └── TimerHistoryAdapter.java # History adapter
│   │   │   │   │   │
│   │   │   │   │   └── base/
│   │   │   │   │       ├── BaseFragment.java       # Base fragment class
│   │   │   │   │       ├── BasePresenter.java      # Base presenter class
│   │   │   │   │       └── BaseContract.java       # Base contract interface
│   │   │   │   │
│   │   │   │   ├── utils/
│   │   │   │   │   ├── Constants.java              # App constants
│   │   │   │   │   ├── TimeUtils.java              # Time formatting utils
│   │   │   │   │   ├── NotificationUtils.java      # Notification helper
│   │   │   │   │   └── PermissionUtils.java        # Permission helper
│   │   │   │   │
│   │   │   │   └── services/
│   │   │   │       ├── TimerService.java           # Background timer service
│   │   │   │       └── NotificationService.java    # Push notifications
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml           # Main activity layout
│   │   │   │   │   ├── activity_splash.xml         # Splash screen layout
│   │   │   │   │   ├── fragment_home.xml           # Home fragment layout
│   │   │   │   │   ├── fragment_timer.xml          # Timer fragment layout
│   │   │   │   │   ├── fragment_statistics.xml     # Statistics layout
│   │   │   │   │   ├── fragment_settings.xml       # Settings layout
│   │   │   │   │   ├── item_study_session.xml      # RecyclerView item
│   │   │   │   │   └── item_timer_history.xml      # History item layout
│   │   │   │   │
│   │   │   │   ├── navigation/
│   │   │   │   │   └── nav_graph.xml               # Navigation graph
│   │   │   │   │
│   │   │   │   ├── menu/
│   │   │   │   │   ├── bottom_nav_menu.xml         # Bottom navigation
│   │   │   │   │   └── main_menu.xml               # Toolbar menu
│   │   │   │   │
│   │   │   │   ├── values/
│   │   │   │   │   ├── themes.xml                  # Material3 themes
│   │   │   │   │   ├── colors.xml                  # Color palette
│   │   │   │   │   ├── strings.xml                 # String resources
│   │   │   │   │   ├── dimens.xml                  # Dimension values
│   │   │   │   │   └── styles.xml                  # Custom styles
│   │   │   │   │
│   │   │   │   ├── values-night/
│   │   │   │   │   ├── themes.xml                  # Dark theme
│   │   │   │   │   └── colors.xml                  # Dark mode colors
│   │   │   │   │
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_timer.xml                # Timer icon
│   │   │   │   │   ├── ic_home.xml                 # Home icon
│   │   │   │   │   ├── ic_statistics.xml           # Stats icon
│   │   │   │   │   ├── ic_settings.xml             # Settings icon
│   │   │   │   │   ├── bg_circle.xml               # Circular background
│   │   │   │   │   └── bg_timer_progress.xml       # Timer progress bg
│   │   │   │   │
│   │   │   │   ├── color/
│   │   │   │   │   ├── bottom_nav_color.xml        # Bottom nav states
│   │   │   │   │   └── button_color.xml            # Button color states
│   │   │   │   │
│   │   │   │   ├── font/
│   │   │   │   │   ├── roboto_regular.ttf          # Custom fonts
│   │   │   │   │   └── roboto_bold.ttf
│   │   │   │   │
│   │   │   │   ├── anim/
│   │   │   │   │   ├── slide_in_right.xml          # Fragment animations
│   │   │   │   │   ├── slide_out_left.xml
│   │   │   │   │   └── fade_in.xml
│   │   │   │   │
│   │   │   │   └── xml/
│   │   │   │       ├── backup_rules.xml            # Backup configuration
│   │   │   │       └── data_extraction_rules.xml   # Data extraction rules
│   │   │   │
│   │   │   └── AndroidManifest.xml                 # App manifest
│   │   │
│   │   ├── test/
│   │   │   └── java/com/example/timerstudy/        # Unit tests
│   │   │       ├── repository/
│   │   │       │   └── UserRepositoryTest.java
│   │   │       └── presenter/
│   │   │           └── HomePresenterTest.java
│   │   │
│   │   └── androidTest/
│   │       └── java/com/example/timerstudy/        # Instrumented tests
│   │           └── ExampleInstrumentedTest.java
│   │
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar                       # Gradle wrapper
│       └── gradle-wrapper.properties               # Gradle properties
│
├── build.gradle                                     # Project-level build
├── gradle.properties                               # Global gradle properties
├── settings.gradle                                 # Project settings
├── local.properties                                # SDK location (gitignored)
└── README.md                                       # This documentation
```

---

## 🔄 Data Flow - MVP Pattern

### 1️⃣ User Interaction → View

```java
// User clicks button in Fragment
binding.btnRefresh.setOnClickListener(v -> {
    presenter.onRefreshClicked(); // Call Presenter
});
```

### 2️⃣ View → Presenter

```java
// Presenter xử lý logic
public void onRefreshClicked() {
    view.showLoading(true);
    repository.getCurrentUser(new DataCallback<User>() {
        @Override
        public void onSuccess(User user) {
            view.showLoading(false);
            view.displayUser(user); // Update View
        }

        @Override
        public void onError(String error) {
            view.showLoading(false);
            view.showError(error);
        }
    });
}
```

### 3️⃣ Presenter → Repository

```java
// Repository lấy data với callback
public void getCurrentUser(DataCallback<User> callback) {
    executorService.execute(() -> {
        try {
            // Simulate API call
            Thread.sleep(500);
            callback.onSuccess(currentUser);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    });
}
```

### 4️⃣ Repository → Presenter → View

```java
// Fragment implement View interface và update UI
@Override
public void displayUser(User user) {
    binding.tvUserName.setText(user.getName());
    binding.tvUserAge.setText("Tuổi: " + user.getAge());
}

@Override
public void showLoading(boolean isLoading) {
    binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
}
```

---

## 🧭 Navigation Flow

```
MainActivity (NavHost)
    │
    ├─> NavHostFragment (Container)
    │       │
    │       ├─> HomeFragment (Start Destination)
    │       │       │
    │       │       ├─> HomePresenter
    │       │       │       │
    │       │       │       └─> UserRepository
    │       │       │
    │       │       └─> Hiển thị thông tin user
    │       │           Via contract interface calls
    │       │
    │       └─> TimerFragment
    │               │
    │               ├─> TimerPresenter
    │               │       │
    │               │       └─> TimerRepository
    │               │
    │               └─> Timer functionality
    │                   Via contract interface
    │
    └─> BottomNavigationView
            │
            └─> Điều hướng giữa các Fragments
                (Auto-handled by NavigationUI)
```

---

## 🚀 Cách Chạy Project

### Bước 1: Import vào Android Studio

1. Mở Android Studio
2. File → New → Import Project
3. Chọn thư mục project
4. Đợi Gradle sync hoàn tất

### Bước 2: Cấu hình

- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Java Version**: 1.8

### Bước 3: Run

- Click **Run** (Shift + F10)
- Chọn emulator hoặc thiết bị thật
- App sẽ khởi động với màn hình HomeFragment

---

## 🎯 Tính Năng Hiện Tại

### ✅ HomeFragment (MVP)

- Implement HomeContract.View interface
- Khởi tạo HomePresenter trong onViewCreated()
- Gọi presenter methods cho user actions
- Hiển thị data qua contract methods
- Clean up presenter trong onDestroyView()

### ✅ TimerFragment (MVP)

- Implement TimerContract.View interface
- Presenter quản lý timer logic và state
- Repository handle timer data persistence
- Clean separation of concerns

### ✅ MainActivity

- Toolbar với Material Design 3
- BottomNavigationView với tabs
- NavController quản lý navigation
- Auto back-stack handling

---

## 🔧 Mở Rộng Project

### 📌 Thêm MVP Screen Mới

#### Bước 1: Tạo Contract Interface

```java
package com.example.timerstudy.ui.fragments.newscreen;

public interface NewScreenContract {

    interface View {
        void showLoading(boolean isLoading);
        void showError(String message);
        void displayData(List<DataModel> data);
        void showSuccess(String message);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadData();
        void onItemClicked(DataModel item);
        void onRefreshRequested();
    }
}
```

#### Bước 2: Implement Presenter

```java
public class NewScreenPresenter implements NewScreenContract.Presenter {
    private NewScreenContract.View view;
    private DataRepository repository;

    public NewScreenPresenter(DataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void attachView(NewScreenContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadData() {
        if (view != null) {
            view.showLoading(true);
        }

        repository.getData(new DataCallback<List<DataModel>>() {
            @Override
            public void onSuccess(List<DataModel> data) {
                if (view != null) {
                    view.showLoading(false);
                    view.displayData(data);
                }
            }

            @Override
            public void onError(String error) {
                if (view != null) {
                    view.showLoading(false);
                    view.showError(error);
                }
            }
        });
    }
}
```

#### Bước 3: Implement Fragment

```java
public class NewScreenFragment extends Fragment implements NewScreenContract.View {
    private FragmentNewScreenBinding binding;
    private NewScreenContract.Presenter presenter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize presenter
        DataRepository repository = new DataRepository();
        presenter = new NewScreenPresenter(repository);
        presenter.attachView(this);

        // Setup UI
        setupUI();

        // Load initial data
        presenter.loadData();
    }

    private void setupUI() {
        binding.btnRefresh.setOnClickListener(v -> presenter.onRefreshRequested());
    }

    @Override
    public void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void displayData(List<DataModel> data) {
        // Update RecyclerView or UI components
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
        binding = null;
    }
}
```

---

## 📚 Best Practices Đã Áp Dụng

✅ **ViewBinding** - Type-safe view access  
✅ **Contract Interfaces** - Clear separation between View and Presenter  
✅ **Repository Pattern** - Single source of truth  
✅ **Callback Pattern** - Async data handling  
✅ **Separation of Concerns** - View chỉ handle UI, Presenter handle logic  
✅ **Material Design 3** - Modern UI components  
✅ **Navigation Component** - Type-safe navigation  
✅ **Resource Management** - Proper cleanup (presenter.detachView())  
✅ **Error Handling** - Centralized error handling trong Presenter  
✅ **Testability** - Presenter không depend vào Android framework

---

## 🔄 MVP vs MVVM Comparison

| Aspect                    | MVVM (Trước)             | MVP (Hiện tại)             |
| ------------------------- | ------------------------ | -------------------------- |
| **Complexity**            | Cao (LiveData, Observer) | Thấp (Interface, Callback) |
| **Learning Curve**        | Steep                    | Gentle                     |
| **Testability**           | Good                     | Excellent                  |
| **Memory Leaks**          | Có thể (Observer)        | Ít (Manual detach)         |
| **Configuration Changes** | Auto handle              | Manual handle              |
| **Data Binding**          | Two-way                  | One-way                    |
| **Code Amount**           | Nhiều hơn                | Ít hơn                     |

---

## 🐛 Troubleshooting

### Issue: Presenter không được detach

**Solution**:

- Luôn gọi `presenter.detachView()` trong `onDestroyView()`
- Check null trước khi call view methods trong presenter

### Issue: Memory leak

**Solution**:

- Implement WeakReference trong Presenter nếu cần
- Đảm bảo detach view properly

### Issue: Callback hell

**Solution**:

- Sử dụng RxJava hoặc Coroutines cho complex async operations
- Chain callbacks properly

---

## 📖 Tài Liệu Tham Khảo

- [Android MVP Guide](https://github.com/googlesamples/android-architecture)
- [MVP vs MVVM](https://medium.com/@ankit.sinhal/mvp-vs-mvvm-android-architecture-patterns-dfb0b6c3f9e2)
- [Navigation Component](https://developer.android.com/guide/navigation)
- [Material Design 3](https://m3.material.io/)
- [ViewBinding](https://developer.android.com/topic/libraries/view-binding)

---

## 👨‍💻 Author

Created as a base template for Android MVP projects.

**Version**: 2.0 (Migrated from MVVM to MVP)  
**Last Updated**: 2025  
**License**: MIT

---

## 🗄️ Database Schema

Project đã được tích hợp Room Database với 10 Entity classes:

### 📊 **Entity Classes**

- **UserEntity** - Quản lý thông tin người dùng
- **SessionEntity** - Theo dõi các phiên học tập/focus
- **SessionPauseEntity** - Ghi lại các lần tạm dừng trong session
- **TaskEntity** - Quản lý danh sách công việc
- **TaskSessionEntity** - Liên kết giữa task và session
- **GoalEntity** - Mục tiêu hàng ngày
- **UserSettingEntity** - Cài đặt cá nhân của user
- **DefaultSettingEntity** - Cài đặt mặc định của app
- **StatisticsCacheEntity** - Cache thống kê để tối ưu hiệu năng
- **StreakRecordEntity** - Theo dõi chuỗi ngày học tập

### 🔧 **Type Converters**

- **DateConverter** - Chuyển đổi Date ↔ Long timestamp

### 📋 **Database Features**

- ✅ Foreign key relationships với CASCADE delete
- ✅ Comprehensive indexing cho performance
- ✅ Type converters cho Date objects
- ✅ Default values và constraints
- ✅ Constants cho status và type values

Chi tiết đầy đủ xem file: [DATABASE_DOCS.md](DATABASE_DOCS.md)

---

## 🎯 Next Steps

- [x] ✅ Migrate từ MVVM sang MVP
- [x] ✅ Thêm Room Database
- [ ] Tích hợp Retrofit cho API calls với MVP pattern
- [ ] Implement Paging với MVP
- [ ] Thêm Unit Tests cho Presenters
- [ ] Thêm Dependency Injection (Dagger2)
- [ ] Implement SharedPreferences replacement
- [ ] Thêm WorkManager cho background tasks
