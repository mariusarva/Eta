package io.github.mangi.eta.ui.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

/** Activity 级状态所有者；配置变更只重建 UI，不替换正在运行的 Agent 会话。 */
internal class AgentAppViewModel(application: Application) : AndroidViewModel(application) {
    val state = AgentAppState(
        context = application,
        scope = viewModelScope,
    )
    /** 续写事务需要活过源会话组合的销毁，因此绑到 viewModelScope。 */
    internal val continueScope: kotlinx.coroutines.CoroutineScope get() = viewModelScope
    val skills = AgentSkillsStore(application, viewModelScope)
    val memory = AgentMemoryStore(application, viewModelScope)
    val permissionHealth = PermissionHealthStore(application, viewModelScope)
    private val terminalHost = TerminalSessionHost.get(application)
    val terminalStore = terminalHost.terminal
    val consoleStore = terminalHost.console
}
