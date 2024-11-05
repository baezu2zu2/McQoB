# McQoB
## 개요
McQoB는 개발자 바쭈(kbazu)가 자신의 마인크래프트 플러그인 개발 편의를 위해 개발한 플러그인입니다
## 사용법
### 커맨드
McQoB는 한국어 커맨드에서 도움말을 자동으로 지정해주는 기능을 가지고 있는데요  
QoBCommand를 호출하셔서 QoBArg 또는 그 자식 객체를 넣으면 자동으로 도움말이 생성됩니다  
예를 들겠습니다
```kotlin
QoBCommand(this, "바쭈플러그인", mutableListOf(
    object: QoBFixedArg("버전", "mcQoB 플러그인의 버전을 출력합니다", true){
        override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
            sender.sendMessage("${helpColor}현재 mcQoB의 버전: ${state.versionStr(version)}")

            return true
        }
    },
    object: QoBFixedArg("색", "mcQoB에 사용되는 채팅 색깔 조합을 출력합니다", true){
        override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
            sender.sendMessage("${helpColor}일반 메세지의 색깔: ${helpColor.name}")
            sender.sendMessage("${commandColor}커맨드 용법 메세지의 색깔: ${commandColor.name}")
            sender.sendMessage("${errorColor}에러 메세지의 색깔: ${errorColor.name}")

            return true
        }
    }.appendArgs(
        object: QoBFixedArg("에러", "mcQoB에 사용되는 에러 메세지의 색깔을 출력합니다", true){
            override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                sender.sendMessage("${errorColor}에러 메세지의 색깔: ${errorColor.name}")

                return true
            }

        }.appendArgs(
            object: QoBColorArg("에러 메세지의 색깔을 정합니다", true, completed = true){
                override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    return super.check(sender, cmd, cmdStr) && sender.isOp()
                }

                override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean {
                    return super.check(sender, cmd, cmdStr, arg) && sender.isOp
                }

                override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    if(result != null){
                        errorColor = result!!

                        sender.sendMessage("${errorColor}에러 메세지의 색이 ${errorColor.name}으로 설정되었습니다")
                        return true
                    }
                    return false
                }
            }
        ),
        object: QoBFixedArg("커맨드", "mcQoB에 사용되는 커맨드 메세지의 색깔을 출력합니다", true){
            override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                sender.sendMessage("${commandColor}커맨드 용법 메세지의 색깔: ${commandColor.name}")

                return true
            }

        }.appendArgs(
            object: QoBColorArg("커맨드 메세지의 색깔을 정합니다", true, completed = true){
                override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    return super.check(sender, cmd, cmdStr) && sender.isOp()
                }

                override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean {
                    return super.check(sender, cmd, cmdStr, arg) && sender.isOp
                }

                override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    if(result != null){
                        commandColor = result!!

                        sender.sendMessage("${commandColor}커맨드 메세지의 색이 ${commandColor.name}으로 설정되었습니다")
                        return true
                    }
                    return false
                }
            }
        ),
        object: QoBFixedArg("일반", "mcQoB에 사용되는 일반 메세지의 색깔을 출력합니다", true){
            override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                sender.sendMessage("${helpColor}일반 메세지의 색깔: ${helpColor.name}")

                return true
            }

        }.appendArgs(
            object: QoBColorArg("일반 메세지의 색깔을 정합니다", true, completed = true){
                override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    return super.check(sender, cmd, cmdStr) && sender.isOp()
                }

                override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean {
                    return super.check(sender, cmd, cmdStr, arg) && sender.isOp
                }

                override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    if(result != null){
                        helpColor = result!!

                        sender.sendMessage("${helpColor}일반 메세지의 색이 ${helpColor.name}으로 설정되었습니다")
                        return true
                    }
                    return false
                }
            }
        )
    )
))
```
이런 식으로 작동합니다
클래스의 멤버들에 대해 설명하곘습니다
#### QoBCommand
- 생성자
  1. plugin  
  plugin.yml에 적혀 있는 JavaPlugin 클래스의 객체를 넣으면 됩니다
  그 이후 plugin.yml에 커맨드를 등록하면 다른 구성(tabcompleter, commandexecuter 등록 등등)은 자동으로 해줍니다
  2. cmdStr  
  커맨드의 string을 의미합니다  
  예를 들겠습니다
  */바쭈플러그인*이라는 커맨드를 등록하려면
  ```kotlin
  QoBCommand(this, "바쭈플러그인", mutableListOf<QoBArg>())
  ```
  이런 식으로 코드를 짜면 됩니다
  3. firstArgs  
  이는 커맨드의 첫 매개변수들을 의미합니다  
  이는 QoBArg의 객체이기에 해당 내용을 밑에서 설명하곘습니다
#### QoBArg
QoBArg는 커맨드에 인수를 넣기 위해 객체를 생성하는 클래스입니다
QoBArg에 의미있는 메서드는 appendArgs밖에 없습니다  
appendArgs는 기존 인수의 뒤에 인수를 추가하기 위해 사용됩니다  
beforeArgs는 자신의 인수의 앞에 있는 모든 인수들을 기록해놓은 속성입니다  
nextArgs는 다음에 나올 수 있는 인수들을 뜻합니다 
##### QoBValuedArg<T>: QoBArg(...)
QoBValuedArg는 사용자로부터 어떤 타입의 입력을 받고 싶을 때 사용합니다  
여기에는 result