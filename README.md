Lux Meter
=========

Lux Meter is plugin for [AWARE](http://www.awareframework.com/) whitch can be also found in Github [aware-client](https://github.com/denzilferreira/aware-client).
This plugin allows you to record light data without draining your battery. You can also use it in real time mode so that you see line graph in real time.


Settings:
---------
- **status_plugin_lux_meter:** Activate / Deactivate plugin
- **frequency_plugin_lux_mete:r** How frequently to activate light sensor
- **samples_plugin_lux_meter:** How many samples to take at time
- **threshold_plugin_lux_mete:** Shows in broadcast if average lux value was over threshold
- **mode_plugin_lux_meter:** Real time or normal mode


Broadcasts
----------
 **ACTION_AWARE_PLUGIN_LUX_METER:** broadcasted after every new average calculation
 - **avg_lux:** average lux value of last calculation
 - **over_threshold:** is the average over threshold

