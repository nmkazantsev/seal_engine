
## Что это? Зачем?
Данный движок приван взять на себя задачи:
 1. там, где это возможно, избавить пользователя от вызовов низкоуровневых и не интуитивный методов API OpenGL
 2. взять на себя всю работу с видео памятью
 3. предосавить набор обьектов движка, сделать  api обьектно - ориентированным
 4. предоставить абстракицию над классом OpenGLRenderer, сделав возможным создание аналогов activity
 5. обеспечить быстрый старт и легкую разработку прототипа за счет большого количества дефолтных функций

Он может работать с любым glSurfaceView, ниже рассмотрено его использование в полноэкранном режиме
 
## Импорт библиотеки в проект
 1. Создаем пустой проект в Android studio (c пустой дефолтной MainActivity) и скачиваем последнюю версию с репозитория (см релизы)
 2. в проекте в папку `app/libs` копируем файл, пкм, add as android library
 3. ждем
 4. ещё ждем
 5. ура. Теперь открываем MainActivity. Нужно из нее открыть главный реднерер, который позже передаст управление движку и вашему коду. Чтобы это сделать, отредактируем activity:
`Engine engine = new Engine(); //we are unable to make it static because it is impossible to use android context in static way`

 перегрузим методы:

 
	@SuppressLint("ClickableViewAccessibility")  
	@Override  
	protected void onCreate(Bundle savedInstanceState) {  
	    super.onCreate(savedInstanceState);  
	  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  
	  requestWindowFeature(Window.FEATURE_NO_TITLE);  
	  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
	  GLSurfaceView v = engine.onCreate(this, unused -> new MainRenderer(), false);  
	  setContentView(v);  
	 assert v != null;  
	  v.setOnTouchListener(this);  
	}  
  
	@Override  
	protected void onPause() {  
	    super.onPause();  
	  engine.onPause();  
	}  
  
	@Override  
	protected void onResume() {  
	    super.onResume();  
	  engine.onResume();  
	}  
	  
	  
	@SuppressLint("ClickableViewAccessibility")  
	@Override  
	public boolean onTouch(View v, MotionEvent event) {  
	    return Engine.onTouch(v, event);  
	}  
	  
	@Override  
	public void onPointerCaptureChanged(boolean hasCapture) {  
	    super.onPointerCaptureChanged(hasCapture);  
	}

 

 6. Теперь создаим класс MainRenderer. Это будет входная точка в наш проект. Навзние класса можно изменить в строке `GLSurfaceView v = engine.onCreate(this, unused -> new MainRenderer(), false);`  Данный класс является классом страницы, он обязательно должен implement `GamePageInterface`. Конструтор странц может быть любым, но для входной точки обязательно наличие пустого (в процем, мы сами вызываем его из MainActivity). После переопределения всех функций движок готов к использованию
 
