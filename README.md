# BLECommand
 * BLEManage ble管理类
  1. initBLE ble的初始化，返回是否初始化成功，如果未初始化成功，则需要授权
  2. startSearchBLE  开始搜索ble设备，默认10秒后停止搜索，返回设备需要去重去空等操作
  3. stopSearchBLE  停止搜索ble设备
  4. connectBLE  连接ble设备
  5. writeData  发送内容
  6. OnBLEListener  ble设备的回调函数
   6.1 deviceFind   设备获取，需要去空和去重的操作
   6.2 deviceState  设备状态，设备连接成功和设备断开连接的回调
   6.3 deviceConnectError  设备连接过程失败，其中包括service，characteristic，descriptor等获取失败
   6.4 getDeviceContent   获取设备内容，设备返回信息接口回调
   6.5 printDeviceState   打印相关状态的回调，包括连接成功，断开连接，发送成功，接收成功等信息
 * BleSppGattAttributes  ble参数管理类
  主要有service，notify_characteristic,write_characteristics,descriptor等参数
