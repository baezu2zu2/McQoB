# McQoB
## 개요
McQoB는 개발자 바쭈(kbazu)가 자신의 마인크래프트 플러그인 개발 편의를 위해 개발한 플러그인입니다
이 github url로 출처를 밝힌다면 아무나 가져다 써도 좋습니다
## Tutorial
### Command.kt
#### QoBCommand
이 클래스는 커맨드를 생성하는 과정을 진행하는 클래스입니다
QoBCommand 생성자를 호출한 이후 QoBArg들을 넣어서 완료하게 됩니다  
##### 기존 인수들과의 차이
- **'도움말' 인수를 자동으로 생성합니다**
  - 도움말 인수는 커맨드의 사용법을 알려주는 인수입니다
- **커맨드를 버킷에 등록하는 과정을 자동으로 수행합니다**
  - plugin.yml엔 등록해야 하지만 그 이후 CommandExecuter와 TabCompleter의 등록은 필요 없습니다
##### 예시
```java
// java
class ExamplePlugin extends JavaPlugin{ 
  @Override 
  void onEnable(){
    // 커맨드 등록
    new QoBCommand(this, "체력", mutableListOf(
      (인수들...)
    ))
  }
}
```
```kotlin
// kotlin
class ExamplePlugin : JavaPlugin(){
  override fun onEnable(){
    // 커맨드 등록
    QoBCommand(this, "체력", mutableListOf(
      (인수들...)
    ))
  }  
}
```
이런 식으로 QoBCommand를 호출하면 커맨드를 만들 수 있습니다  
여기까지 보면 별로 나은 건 없어 보이지만 **QoBArg**들이 추가된다면 달라집니다
#### QoBArg
QoBArg는 추상 클래스입니다  
많은 형태로 구현될 수 있지만, 기본적인 형태가 구현되어 있는 것들이 많습니다

```java
import com.kbazu.mcQoB.QoBFixedArg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

// java
class ExamplePlugin extends JavaPlugin {
  @Override
  void onEnable() {
    // 커맨드 등록
    new QoBCommand(this, "체력", mutableListOf(
      new QoBFixedArg("채우기", "체력을 채웁니다") {
        @Override
        public boolean run(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String cmdStr) {
          if(sender instanceof Player){
            Player player = ((Player)sender);
            player
          }
            
          return true;
        }
      }
    ));
  }
}
```
```kotlin
// kotlin
class ExamplePlugin : JavaPlugin(){
  override fun onEnable(){
    // 커맨드 등록
    QoBCommand(this, "체력", mutableListOf(
      QoBFixedArg(){
          
      }
    ))
  }  
}
```