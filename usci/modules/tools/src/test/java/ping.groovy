/**
 * Created by emles on 21.09.17
 */

final Process proc = 'ping -c 1 129.0.2.2'.execute()
proc.waitFor()
println proc.text
println proc.exitValue()



