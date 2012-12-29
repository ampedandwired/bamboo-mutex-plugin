Bamboo Mutex Plugin
===================

This is an [Atlassian Bamboo](http://www.atlassian.com/software/bamboo) plugin that allows you to make two or more plans "mutually exclusive". That means that those plans will never be run at the same time, even if agents are available to run them. This is useful if you have shared resources that can only be accessed by one plan at a time.


Installation
------------

Use the [Bamboo plugin manager](https://confluence.atlassian.com/display/BAMBOO/Add-ons) to install the latest version of this plugin from the project's [maven repository]().

You may have to restart Bamboo after installing the plugin.


Usage
-----

The plugin is configured on the "Miscellaneous" tab of the plan configuration. There is a single text entry field that allows you to specify the mutex "key" for the plan. If two or more plans have the same mutex key, this plugin will prevent them from running simultaneously.

Note that specifying multiple keys on a single plan is not supported right now (could be done as an enhancement if there is demand).


Troubleshooting
---------------

On very rare occasions I have observed plans becoming "stuck" - two plans waiting for each other with neither of them being executed. Simply stopping one of them allows things to proceed.


Building From Source
--------------------

This is built using the standard Atlassian SDK. Full instructions on how to set up and use the SDK are available in the [Atlassian plugin SDK documentation](https://developer.atlassian.com/display/DOCS/Introduction+to+the+Atlassian+Plugin+SDK).
