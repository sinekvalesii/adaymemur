# kotlinx.serialization: @Serializable sınıfları koru
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class com.selcukyus.adaymemur.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.selcukyus.adaymemur.**$$serializer { *; }
