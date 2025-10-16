# 🚀 My Base App - Android MVVM Project

## 📖 Giới Thiệu

Base Android project frontend hoàn chỉnh sử dụng:

- **Kiến trúc**: MVVM (Model-View-ViewModel)
- **Navigation**: Navigation Component với BottomNavigationView
- **UI**: Material Design 3
- **Binding**: ViewBinding (type-safe, hiệu năng cao)
- **Ngôn ngữ**: Java

---

## 🏗️ Kiến Trúc MVVM

```
┌─────────────────────────────────────────────┐
│                   VIEW LAYER                │
│  (Activity, Fragments, XML Layouts)         │
│  - Hiển thị UI                              │
│  - Nhận user input                          │
│  - Observe LiveData                         │
└──────────────────┬──────────────────────────┘
                   │ observes
                   │ LiveData
                   ▼
┌─────────────────────────────────────────────┐
│              VIEWMODEL LAYER                │
│  (MainViewModel)                            │
│  - Quản lý UI state                         │
│  - Business logic                           │
│  - Expose LiveData                          │
│  - Survive config changes                   │
└──────────────────┬──────────────────────────┘
                   │ calls
                   │ methods
                   ▼
┌─────────────────────────────────────────────┐
│             REPOSITORY LAYER                │
│  (UserRepository)                           │
│  - Single source of truth                   │
│  - Data operations                          │
│  - API calls / Database                     │
└──────────────────┬──────────────────────────┘
                   │ returns
                   │ data
                   ▼
┌─────────────────────────────────────────────┐
│               MODEL LAYER                   │
│  (User)                                     │
│  - Data classes                             │
│  - Plain Java objects                       │
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
│   │   │   │   │       │   │   └── TimerDao.java  # Timer data access
│   │   │   │   │       │   └── entities/
│   │   │   │   │       │       ├── UserEntity.java
│   │   │   │   │       │       └── TimerEntity.java
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
│   │   │   │   │   │   │   └── HomeViewModel.java  # Home viewmodel
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── timer/
│   │   │   │   │   │   │   ├── TimerFragment.java  # Timer functionality
│   │   │   │   │   │   │   └── TimerViewModel.java # Timer viewmodel
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── statistics/
│   │   │   │   │   │   │   ├── StatisticsFragment.java # Study stats
│   │   │   │   │   │   │   └── StatisticsViewModel.java
│   │   │   │   │   │   │
│   │   │   │   │   │   └── settings/
│   │   │   │   │   │       ├── SettingsFragment.java # App settings
│   │   │   │   │   │       └── SettingsViewModel.java
│   │   │   │   │   │
│   │   │   │   │   ├── adapters/
│   │   │   │   │   │   ├── StudySessionAdapter.java # RecyclerView adapter
│   │   │   │   │   │   └── TimerHistoryAdapter.java # History adapter
│   │   │   │   │   │
│   │   │   │   │   └── base/
│   │   │   │   │       ├── BaseFragment.java       # Base fragment class
│   │   │   │   │       └── BaseViewModel.java      # Base viewmodel class
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
│   │   │       └── viewmodel/
│   │   │           └── HomeViewModelTest.java
│   │   │
│   │   └── androidTest/
│   │       └── java/com/example/timerstudy/        # Instrumented tests
│   │           └── ExampleInstrumentedTest.java
│   │
│   ├── build.gradle                                 # App-level build config
│   └── proguard-rules.pro                          # ProGuard rules
│
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

## 🔄 Data Flow - MVVM Pattern

### 1️⃣ User Interaction → View

```java
// User clicks button in Fragment
binding.btnRefresh.setOnClickListener(v -> {
    viewModel.refreshUserData(); // Call ViewModel
});
```

### 2️⃣ View → ViewModel

```java
// ViewModel xử lý logic
public void refreshUserData() {
    loadingLiveData.postValue(true);
    executorService.execute(() -> {
        User user = repository.getCurrentUser();
        userLiveData.postValue(user); // Update LiveData
    });
}
```

### 3️⃣ ViewModel → Repository

```java
// Repository lấy data (mock hoặc từ API)
public User getCurrentUser() {
    // Simulate API call
    Thread.sleep(500);
    return currentUser;
}
```

### 4️⃣ Repository → ViewModel → View

```java
// Fragment observe LiveData và update UI
viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
    if (user != null) {
        binding.tvUserName.setText(user.getName());
        binding.tvUserAge.setText("Tuổi: " + user.getAge());
    }
});
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
    │       │       └─> Hiển thị thông tin user
    │       │           Observe LiveData từ ViewModel
    │       │
    │       └─> ProfileFragment
    │               │
    │               └─> Form chỉnh sửa user
    │                   Update qua ViewModel
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

### ✅ HomeFragment

- Hiển thị thông tin user trong Material Card
- Avatar circle với ShapeableImageView
- Bio section với CardView
- Button refresh với loading state
- Progress indicator khi loading

### ✅ ProfileFragment

- Form chỉnh sửa với Material TextInputLayout
- Validation real-time
- Character counter cho Bio (max 200)
- Hiển thị thông tin hiện tại
- Update qua ViewModel

### ✅ MainActivity

- Toolbar với Material Design 3
- BottomNavigationView với 2 tabs
- NavController quản lý navigation
- Auto back-stack handling

---

## 🔧 Mở Rộng Project

### 📌 Thêm Fragment Mới

#### Bước 1: Tạo Fragment Class

```java
package com.example.mybaseapp.view.fragments;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private MainViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

#### Bước 2: Tạo Layout XML

```xml
<!-- res/layout/fragment_settings.xml -->
<androidx.constraintlayout.widget.ConstraintLayout>
    <!-- Your UI here -->
</androidx.constraintlayout.widget.ConstraintLayout>
```

#### Bước 3: Thêm vào Navigation Graph

```xml
<!-- res/navigation/nav_graph.xml -->
<fragment
    android:id="@+id/settingsFragment"
    android:name="com.example.mybaseapp.view.fragments.SettingsFragment"
    android:label="Cài Đặt"
    tools:layout="@layout/fragment_settings" />
```

#### Bước 4: Thêm vào Bottom Navigation

```xml
<!-- res/menu/bottom_nav_menu.xml -->
<item
    android:id="@+id/settingsFragment"
    android:icon="@android:drawable/ic_menu_preferences"
    android:title="Cài Đặt" />
```

### 📌 Thêm RecyclerView Adapter

```java
package com.example.mybaseapp.view.adapters;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate item layout
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        void bind(User user) {
            // Bind data to views
        }
    }
}
```

### 📌 Thêm API Call (Retrofit)

#### 1. Thêm dependencies

```gradle
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
```

#### 2. Tạo API Interface

```java
public interface ApiService {
    @GET("users/{id}")
    Call<User> getUser(@Path("id") int userId);
}
```

#### 3. Update Repository

```java
public class UserRepository {
    private ApiService apiService;

    public void fetchUserFromApi(int userId, Callback callback) {
        apiService.getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                callback.onSuccess(response.body());
            }
        });
    }
}
```

---

## 🎨 Customization

### Thay Đổi Theme Color

```xml
<!-- res/values/colors.xml -->
<color name="md_theme_primary">#YOUR_COLOR</color>
```

### Thay Đổi Font

```xml
<!-- res/values/themes.xml -->
<item name="fontFamily">@font/your_custom_font</item>
```

### Dark Mode

Material3 tự động hỗ trợ dark mode. Tạo file:

```
res/values-night/colors.xml
```

---

## 📚 Best Practices Đã Áp Dụng

✅ **ViewBinding** - Type-safe view access  
✅ **LiveData** - Lifecycle-aware data observation  
✅ **ViewModel** - Survive configuration changes  
✅ **Repository Pattern** - Single source of truth  
✅ **Separation of Concerns** - Clear layer separation  
✅ **Material Design 3** - Modern UI components  
✅ **Navigation Component** - Type-safe navigation  
✅ **Resource Management** - Proper cleanup in onDestroyView  
✅ **Error Handling** - Try-catch và error LiveData  
✅ **Input Validation** - Form validation với error display

---

## 🐛 Troubleshooting

### Issue: ViewBinding not generated

**Solution**:

1. Clean Project (Build → Clean Project)
2. Rebuild Project (Build → Rebuild Project)
3. Invalidate Caches (File → Invalidate Caches)

### Issue: Navigation error

**Solution**:

- Kiểm tra tên fragment trong nav_graph.xml khớp với class name
- Đảm bảo fragment IDs trong menu khớp với nav_graph

### Issue: ViewModel not retaining data

**Solution**:

- Sử dụng `ViewModelProvider(requireActivity())` để share ViewModel
- Không khởi tạo ViewModel bằng `new MainViewModel()`

---

## 📖 Tài Liệu Tham Khảo

- [Android MVVM Guide](https://developer.android.com/topic/architecture)
- [Navigation Component](https://developer.android.com/guide/navigation)
- [Material Design 3](https://m3.material.io/)
- [ViewBinding](https://developer.android.com/topic/libraries/view-binding)
- [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)

---

## 👨‍💻 Author

Created as a base template for Android MVVM projects.

**Version**: 1.0  
**Last Updated**: 2025  
**License**: MIT

---

## 🎯 Next Steps

- [ ] Thêm Room Database
- [ ] Tích hợp Retrofit cho API calls
- [ ] Implement Paging 3 cho lists
- [ ] Thêm Unit Tests
- [ ] Thêm Dependency Injection (Hilt/Dagger)
- [ ] Implement DataStore cho preferences
- [ ] Thêm WorkManager cho background tasks
