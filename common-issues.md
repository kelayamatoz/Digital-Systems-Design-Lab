## FAQs

### Running into Issues When Regenerating Design
Sometimes it is possible that you change your design multiple times and want to
generate bitstream for every single iteration of them. Currently we do not have
a mechanism for version control. If you made some changes to your old design and
want to regenerate the design files, you should either remove the old design
folder in `./gen`, or put your changes into a new design and name it
differently. 

### Observing Error Information about Spatial IR when Running Bitstream Generation
Sometimes when running `make vcs` or `make $TARGET`, you would observe some errors that look like: 
```bash
[error] /home/tianzhao/ee109-teaching/spatial-lang-ee109/gen/DotProduct_1_1_16_Int/chisel/x1279.scala:10: not found: value x1278
[error]   x1278.r := (b704 + 16.FP(true, 32, 0)).r
[error]   ^
[error] /home/tianzhao/ee109-teaching/spatial-lang-ee109/gen/DotProduct_1_1_16_Int/chisel/x1279.scala:10: not found: value b704
[error]   x1278.r := (b704 + 16.FP(true, 32, 0)).r
[error]               ^
[error] /home/tianzhao/ee109-teaching/spatial-lang-ee109/gen/DotProduct_1_1_16_Int/chisel/x1296.scala:10: not found: value x1295
[error]   x1295.r := (b735 + 16.FP(true, 32, 0)).r
[error]   ^
[error] /home/tianzhao/ee109-teaching/spatial-lang-ee109/gen/DotProduct_1_1_16_Int/chisel/x1296.scala:10: not found: value b735
[error]   x1295.r := (b735 + 16.FP(true, 32, 0)).r
...
```
This usually happens because you have an old design generated from Spatial, and when creating the new design you forgot to remove the old design. You can resolve this issue by removing the old design and regenerate it. 

### Could Not Find the Generated Bitstream
This is usually due to the fact that your design is so large it cannot fit on
board. You can find a few reports under the verilog-arria/output_files folder of your
generated design directory, which can tell you if your design is too large. 

### Could Not Generate the Design Due To Parallelization Factors
This message `[error] (run-main-0) java.lang.IndexOutOfBoundsException: 500` is usually related to the issue that you are parallelizing your design by too much. To resolve this issue, you should try reducing the parallelization factors.
```bash
[Fringe] loadStreamInfo: List(StreamParInfo(32,16,31,false), StreamParInfo(32,16,30,false), StreamParInfo(32,16,29,false), StreamParInfo(32,16,28,false), StreamParInfo(32,16,27,false), StreamParInfo(32,16,26,false), StreamParInfo(32,16,25,false), StreamParInfo(32,16,24,false), StreamParInfo(32,16,23,false), StreamParInfo(32,16,22,false), StreamParInfo(32,16,21,false), StreamParInfo(32,16,20,false), StreamParInfo(32,16,19,false), StreamParInfo(32,16,18,false), StreamParInfo(32,16,17,false), StreamParInfo(32,16,16,false), StreamParInfo(32,16,15,false), StreamParInfo(32,16,14,false), StreamParInfo(32,16,13,false), StreamParInfo(32,16,12,false), StreamParInfo(32,16,11,false), StreamParInfo(32,16,10,false), StreamParInfo(32,16,9,false), StreamParInfo(32,16,8,false), StreamParInfo(32,16,7,false), StreamParInfo(32,16,6,false), StreamParInfo(32,16,5,false), StreamParInfo(32,16,4,false), StreamParInfo(32,16,3,false), StreamParInfo(32,16,2,false), StreamParInfo(32,16,1,false), StreamParInfo(32,16,0,false)), storeStreamInfo: List(StreamParInfo(32,16,0,false))
[Fringe] Creating MAG 0, assignment: List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32), loadStreamIDs: List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31), storeStreamIDs: List(0)
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[warn] FF of width 0 detected!
[error] (run-main-0) java.lang.IndexOutOfBoundsException: 500
java.lang.IndexOutOfBoundsException: 500
	at scala.collection.immutable.Vector.checkRangeConvert(Vector.scala:132)
	at scala.collection.immutable.Vector.apply(Vector.scala:122)
	at chisel3.core.Vec.apply(Aggregate.scala:221)
	at fringe.MAGCore.connectDbgSig(MAGCore.scala:107)
	at fringe.MAGCore$$anonfun$60.apply(MAGCore.scala:499)
	at fringe.MAGCore$$anonfun$60.apply(MAGCore.scala:498)
	at scala.collection.immutable.List.foreach(List.scala:381)
	at fringe.MAGCore.<init>(MAGCore.scala:498)
	at fringe.Fringe$$anonfun$4$$anonfun$9.apply(Fringe.scala:114)
	at fringe.Fringe$$anonfun$4$$anonfun$9.apply(Fringe.scala:114)
	at chisel3.core.Module$.do_apply(Module.scala:45)
	at fringe.Fringe$$anonfun$4.apply(Fringe.scala:114)
	at fringe.Fringe$$anonfun$4.apply(Fringe.scala:104)
	at scala.collection.generic.GenTraversableFactory.tabulate(GenTraversableFactory.scala:148)
	at fringe.Fringe.<init>(Fringe.scala:104)
	at top.Top$$anonfun$2.apply(Top.scala:216)
	at top.Top$$anonfun$2.apply(Top.scala:216)
	at chisel3.core.Module$.do_apply(Module.scala:45)
	at top.Top.<init>(Top.scala:216)
	at top.Instantiator$$anonfun$dut$1.apply(Instantiator.scala:201)
	at top.Instantiator$$anonfun$dut$1.apply(Instantiator.scala:22)
	at chisel3.core.Module$.do_apply(Module.scala:45)
	at chisel3.Driver$$anonfun$elaborate$1.apply(Driver.scala:92)
	at chisel3.Driver$$anonfun$elaborate$1.apply(Driver.scala:92)
	at chisel3.internal.Builder$$anonfun$build$1.apply(Builder.scala:240)
	at chisel3.internal.Builder$$anonfun$build$1.apply(Builder.scala:238)
	at scala.util.DynamicVariable.withValue(DynamicVariable.scala:58)
	at chisel3.internal.Builder$.build(Builder.scala:238)
	at chisel3.Driver$.elaborate(Driver.scala:92)
	at chisel3.Driver$.execute(Driver.scala:139)
	at chisel3.Driver$.execute(Driver.scala:200)
	at fringe.CommonMain$class.main(CommonMain.scala:64)
	at top.Instantiator$.main(Instantiator.scala:19)
	at top.Instantiator.main(Instantiator.scala)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
[trace] Stack trace suppressed: run last compile:runMain for the full output.
java.lang.RuntimeException: Nonzero exit code: 1
	at scala.sys.package$.error(package.scala:27)
[trace] Stack trace suppressed: run last compile:runMain for the full output.
[error] (compile:runMain) Nonzero exit code: 1
[error] Total time: 424 s, completed Mar 28, 2018 5:33:39 PM
make: *** [vcs-hw] Error 1
```
