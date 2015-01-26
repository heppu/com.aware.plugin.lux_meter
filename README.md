Lux Meter
=========

Lux meter is plugin for [AWARE](http://www.awareframework.com/) whitch can be also found in Github [aware-client](https://github.com/denzilferreira/aware-client).
With this plugin you can measure amount of light with real time graph. You can also do long time light data recording with great battery efficiency.

Settings:
---------
- **status_plugin_lux_meter:** Activate / Deactivate plugin
- **frequency_plugin_lux_meter:**  How frequently to check light conditions
- **samples_plugin_lux_meter:** How many samples to take once in frequency
- **threshold_plugin_lux_meter:**  Average level that is beneficial
- **mode_plugin_lux_meter:**  Real time or normal mode


Broadcasts
----------
- **ACTION_AWARE_PLUGIN_LUX_METER:** Broadcasted after every new average calculation
  - **avg_lux:** average lux value after previous broadcast
  - **over_threshold:** is the average over threshold


Providers
----------
**URI:** content://com.aware.plugin.lux_meter.provider.lux_meter/plugin_lux_meter

| Table field      | Field type | Description                   |
| ---------------- |:----------:| -----------------------------:|
| _id              | INTEGER    | primary key autoincrement     |
| timestamp        | REAL       | unix timestamp                |
| device_id        | TEXT       | AWARE device id               |
| double_frequency | REAL       | time between avg calculations |
| over_threshold   | INTEGER    | 0=under 1=over                |
| lux_avg          | INTEGER    | average lux value             |
| light_threshold  | INTEGER    | threshold that was used       |
