;-00---- BEGIN CORE.LL -----------------------------------------------------------------------------;
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~;
; Core functions and definitions ;
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~;
; the maximum size of a input string
; needed external functions
declare i8* @malloc(i32)
declare i8* @realloc(i8*, i32) ; array reallocation
declare void @abort() ; interrupt execution in case of errors
declare i32 @strlen(i8*)
declare i8* @strcpy(i8*, i8*)
declare i8* @strcat(i8*, i8*)
declare i32 @strcmp(i8*, i8*)
declare i32 @printf(i8* noalias nocapture, ...) ; outut
declare ptr @fgets(ptr noundef, i32 noundef, ptr noundef) ; input
; a box holds a type and a value, the value is i64 to contain a double
; if the type is 0 or the box is considered a null value.
; In all other comments, if it is not explicitly specified, every occurrence of null is to be interpreted as a box with type 0.
%struct.Boxed = type {
 i3, ; type
 i64 ; value
}
; Get the type of a box, must be non-null
define i3 @_GET_TYPE(%struct.Boxed* %this) {
 %struct.type.ptr = getelementptr %struct.Boxed, %struct.Boxed* %this, i32 0, i32 0
 %type = load i3, i3* %struct.type.ptr
 ret i3 %type
}
define void @_SET_TYPE(%struct.Boxed* %this, i3 %type) {
 %struct.type.ptr = getelementptr %struct.Boxed, %struct.Boxed* %this, i32 0, i32 0
 store i3 %type, i3* %struct.type.ptr
 ret void
}
; Copy a box into another
; array copy is not supported
; from must be non null
define void @_COPY(%struct.Boxed* %to, %struct.Boxed* %from) {
 %type = call i3 @_GET_TYPE(%struct.Boxed* %from)
 switch i3 %type, label %otherwise [ i3 1, label %number.type
                                      i3 2, label %string.type
           i3 3, label %bool.type ]
number.type:
 %f.value = call double @_GET_NUM_VALUE(%struct.Boxed* %from)
 call void @_SET_NUM_VALUE(%struct.Boxed* %to, double %f.value)
 ret void
string.type:
 %s.value = call i8* @_GET_STR_VALUE(%struct.Boxed* %from)
 %s.len = call i32 @strlen(i8* %s.value)
 %s.len.1 = add i32 %s.len, 1
 %new.str = call i8* @malloc(i32 %s.len.1) ; memory leak
 call i8* @strcpy(i8* %new.str, i8* %s.value)
 call void @_SET_STR_VALUE(%struct.Boxed* %to, i8* %new.str)
 ret void
bool.type:
 %b.value = call i1 @_GET_BOOL_VALUE(%struct.Boxed* %from)
 call void @_SET_BOOL_VALUE(%struct.Boxed* %to, i1 %b.value)
 ret void
otherwise:
 switch i3 %type, label %end [ i3 4, label %array.type ]
array.type:
 call void @_ARRAY_COPY_E()
 ret void
end:
 call void @_UNKNOWN_ERROR()
 ret void
}
; assign a default value to a box based on the type given
define void @_DEFAULT_IF_NULL(%struct.Boxed* %this, i3 %type) {
 %value.type = call i3 @_GET_TYPE(%struct.Boxed* %this)
 %bool = icmp eq i3 %value.type, 0
 br i1 %bool, label %is.null, label %end
is.null:
 ; for some reason if i add a fourth element to the switch i get a linking error that i am
 ; too dumb to fix
 switch i3 %type, label %otherwise [ i3 1, label %number.type
                                            i3 2, label %string.type
                i3 3, label %bool.type ]
number.type:
 call void @_SET_NUM_VALUE(%struct.Boxed* %this, double 0.0)
 ret void
string.type:
 %empty = call i8* @malloc(i32 1) ; memory leak
 store i8 0, i8* %empty
 call void @_SET_STR_VALUE(%struct.Boxed* %this, i8* %empty)
 ret void
bool.type:
 call void @_SET_BOOL_VALUE(%struct.Boxed* %this, i1 0)
 ret void
otherwise:
 switch i3 %type, label %end [ i3 4, label %array.type ]
array.type:
 call void @_EMPTY_ARRAY(%struct.Boxed* %this)
 ret void
end:
 ret void
}
;-00---- END CORE.LL -------------------------------------------------------------------------------;
;-01---- BEGIN NUMBER.LL ---------------------------------------------------------------------------;
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~;
; Functions to deal with numbers ;
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~;
; THESE FUNCTIONS DO NOT CHECK IF THE VALUE IS OF THE RIGHT KIND
; IT IS THE CALEE RESPONSABILITY
; number getter
define double @_GET_NUM_VALUE(%struct.Boxed* %value) {
 %value.ptr = getelementptr %struct.Boxed, %struct.Boxed* %value, i32 0, i32 1 ; extract the pointer to the number from the struct
 %i.value = load i64, i64* %value.ptr ; extract the number from the pointer
 %f.value = bitcast i64 %i.value to double ; cast to a double
 ret double %f.value
}
; number setter
define void @_SET_NUM_VALUE(%struct.Boxed* %self, double %value) {
 %type.ptr = getelementptr %struct.Boxed, %struct.Boxed* %self, i32 0, i32 0 ; extract the pointer to the bool from the struct
 %value.ptr = getelementptr %struct.Boxed, %struct.Boxed* %self, i32 0, i32 1 ; extract the pointer to the bool from the struct
 %b.value = bitcast double %value to i64 ; cast to a i64
 store i3 1, i3* %type.ptr ; insert the type in the result
 store i64 %b.value, i64* %value.ptr ; insert the value in the result
 ret void
}
; `left' and `right' must be numbers, `left' and `right' may be null
define void @MINUS(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 1)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 %left.float = call double @_GET_NUM_VALUE(%struct.Boxed* %left)
 %right.float = call double @_GET_NUM_VALUE(%struct.Boxed* %right)
 %res.value = fsub double %left.float, %right.float
 call void @_SET_NUM_VALUE(%struct.Boxed* %res, double %res.value)
 ret void
}

define void @MULT(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 1)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 %left.float = call double @_GET_NUM_VALUE(%struct.Boxed* %left)
 %right.float = call double @_GET_NUM_VALUE(%struct.Boxed* %right)
 %res.value = fmul double %left.float, %right.float
 call void @_SET_NUM_VALUE(%struct.Boxed* %res, double %res.value)
 ret void
}

define void @NUM_PLUS(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 1)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 %left.float = call double @_GET_NUM_VALUE(%struct.Boxed* %left)
 %right.float = call double @_GET_NUM_VALUE(%struct.Boxed* %right)
 %res.value = fadd double %left.float, %right.float
 call void @_SET_NUM_VALUE(%struct.Boxed* %res, double %res.value)
 ret void
}

; `value' must be a number, `value' may be null
define void @UNARY_MINUS(%struct.Boxed* %res, %struct.Boxed* %value) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %value, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %value, i3 1)
 %float = call double @_GET_NUM_VALUE(%struct.Boxed* %value)
 %m.float = fsub double 0.0, %float
 call void @_SET_NUM_VALUE(%struct.Boxed* %res, double %m.float)
 ret void
}
; an overloaded version of plus that calls `@CONCAT' or `@NUM_PLUS' based on the types of the operands.
; if the first argument is null it checks the type of the second
; if the second is also null it throw an exception (TODO: maybe make it call `@NUM_PLUS')
; Fix the dealing with nulls and numbers
define void @PLUS(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 %type.left = call i3 @_GET_TYPE(%struct.Boxed* %left)
 switch i3 %type.left, label %otherwise [ i3 0, label %null.type
                                                 i3 1, label %number.type
                                                 i3 2, label %string.type ]
null.type:
 %type.right = call i3 @_GET_TYPE(%struct.Boxed* %right)
 switch i3 %type.right, label %otherwise [ i3 1, label %number.type
                                                  i3 2, label %string.type ]
number.type:
 call void @NUM_PLUS(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right)
 ret void
string.type:
 call void @CONCAT(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right)
 ret void
otherwise:
 call void @_NUM_OR_STR_E(%struct.Boxed* %left)
 call void @_NUM_OR_STR_E(%struct.Boxed* %right)
 ret void
}
; `left' and `right' must be numbers, `left' and `right' may be null, `right' must not be zero
define void @DIV(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 1)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 call void @_CHECK_ZERO_DIV_E(%struct.Boxed* %right)
 %left.float = call double @_GET_NUM_VALUE(%struct.Boxed* %left)
 %right.float = call double @_GET_NUM_VALUE(%struct.Boxed* %right)
 %res.value = fdiv double %left.float, %right.float
 call void @_SET_NUM_VALUE(%struct.Boxed* %res, double %res.value)
 ret void
}
; `value' must be a number, `value' may be null
define i64 @_FLOOR(%struct.Boxed* %value) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %value, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %value, i3 1)
 %f.value = call double @_GET_NUM_VALUE(%struct.Boxed* %value)
 %res.value = fptoui double %f.value to i64
 ret i64 %res.value
}
;-01---- END NUMBER.LL -----------------------------------------------------------------------------;
;-02---- BEGIN BOOL.LL -----------------------------------------------------------------------------;
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~;
; Functions to deal with booleans and comparisons ;
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~;
define i1 @_GET_BOOL_VALUE(%struct.Boxed* %value) {
 %value.ptr = getelementptr %struct.Boxed, %struct.Boxed* %value, i32 0, i32 1 ; extract the pointer to the bool from the struct
 %i.value = load i64, i64* %value.ptr ; extract the bool from the pointer
 %b.value = trunc i64 %i.value to i1 ; truncate the value to one bit
 ret i1 %b.value
}
define void @_SET_BOOL_VALUE(%struct.Boxed* %self, i1 %value) {
 %type.ptr = getelementptr %struct.Boxed, %struct.Boxed* %self, i32 0, i32 0 ; extract the pointer to the bool from the struct
 %value.ptr = getelementptr %struct.Boxed, %struct.Boxed* %self, i32 0, i32 1 ; extract the pointer to the bool from the struct
 %i.value = sext i1 %value to i64
 store i3 3, i3* %type.ptr
 store i64 %i.value, i64* %value.ptr
 ret void
}
; Create a function named `name', that compares two numbers using `fop' or compares two string using
; `op' (strcmp(str1, str), 0) otherwise.
; Both operands may be null, but must be of the same type.
; @param res the result pointer
; @param left the left operand
; @param right the right operand
define void @GEQ(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 %type.left = call i3 @_GET_TYPE(%struct.Boxed* %left)
 switch i3 %type.left, label %otherwise [ i3 0, label %null.type
 i3 1, label %number.type
 i3 2, label %string.type ]
null.type:
 %type.right = call i3 @_GET_TYPE(%struct.Boxed* %right)
 switch i3 %type.right, label %otherwise [ i3 1, label %number.type
 i3 2, label %string.type ]
number.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 1)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 %f.value.left = call double @_GET_NUM_VALUE(%struct.Boxed* %left)
 %f.value.right = call double @_GET_NUM_VALUE(%struct.Boxed* %right)
 %f.bool = fcmp oge double %f.value.left, %f.value.right
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %f.bool)
 ret void
string.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 2)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 2)
 %left.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %left)
 %right.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %right)
 %strcmp = call i32 @strcmp(i8* %left.str, i8* %right.str)
 %s.bool = icmp sge i32 %strcmp, 0
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %s.bool)
 ret void
otherwise:
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 ret void
}

define void @LEQ(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 %type.left = call i3 @_GET_TYPE(%struct.Boxed* %left)
 switch i3 %type.left, label %otherwise [ i3 0, label %null.type
 i3 1, label %number.type
 i3 2, label %string.type ]
null.type:
 %type.right = call i3 @_GET_TYPE(%struct.Boxed* %right)
 switch i3 %type.right, label %otherwise [ i3 1, label %number.type
 i3 2, label %string.type ]
number.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 1)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 %f.value.left = call double @_GET_NUM_VALUE(%struct.Boxed* %left)
 %f.value.right = call double @_GET_NUM_VALUE(%struct.Boxed* %right)
 %f.bool = fcmp ole double %f.value.left, %f.value.right
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %f.bool)
 ret void
string.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 2)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 2)
 %left.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %left)
 %right.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %right)
 %strcmp = call i32 @strcmp(i8* %left.str, i8* %right.str)
 %s.bool = icmp sle i32 %strcmp, 0
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %s.bool)
 ret void
otherwise:
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 ret void
}

define void @LT(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 %type.left = call i3 @_GET_TYPE(%struct.Boxed* %left)
 switch i3 %type.left, label %otherwise [ i3 0, label %null.type
 i3 1, label %number.type
 i3 2, label %string.type ]
null.type:
 %type.right = call i3 @_GET_TYPE(%struct.Boxed* %right)
 switch i3 %type.right, label %otherwise [ i3 1, label %number.type
 i3 2, label %string.type ]
number.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 1)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 %f.value.left = call double @_GET_NUM_VALUE(%struct.Boxed* %left)
 %f.value.right = call double @_GET_NUM_VALUE(%struct.Boxed* %right)
 %f.bool = fcmp olt double %f.value.left, %f.value.right
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %f.bool)
 ret void
string.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 2)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 2)
 %left.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %left)
 %right.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %right)
 %strcmp = call i32 @strcmp(i8* %left.str, i8* %right.str)
 %s.bool = icmp slt i32 %strcmp, 0
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %s.bool)
 ret void
otherwise:
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 ret void
}

define void @GT(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 %type.left = call i3 @_GET_TYPE(%struct.Boxed* %left)
 switch i3 %type.left, label %otherwise [ i3 0, label %null.type
 i3 1, label %number.type
 i3 2, label %string.type ]
null.type:
 %type.right = call i3 @_GET_TYPE(%struct.Boxed* %right)
 switch i3 %type.right, label %otherwise [ i3 1, label %number.type
 i3 2, label %string.type ]
number.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 1)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 %f.value.left = call double @_GET_NUM_VALUE(%struct.Boxed* %left)
 %f.value.right = call double @_GET_NUM_VALUE(%struct.Boxed* %right)
 %f.bool = fcmp ogt double %f.value.left, %f.value.right
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %f.bool)
 ret void
string.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 2)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 2)
 %left.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %left)
 %right.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %right)
 %strcmp = call i32 @strcmp(i8* %left.str, i8* %right.str)
 %s.bool = icmp sgt i32 %strcmp, 0
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %s.bool)
 ret void
otherwise:
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 ret void
}

define void @SAME_EQ(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 %type.left = call i3 @_GET_TYPE(%struct.Boxed* %left)
 switch i3 %type.left, label %otherwise [ i3 0, label %null.type
 i3 1, label %number.type
 i3 2, label %string.type ]
null.type:
 %type.right = call i3 @_GET_TYPE(%struct.Boxed* %right)
 switch i3 %type.right, label %otherwise [ i3 1, label %number.type
 i3 2, label %string.type ]
number.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 1)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 %f.value.left = call double @_GET_NUM_VALUE(%struct.Boxed* %left)
 %f.value.right = call double @_GET_NUM_VALUE(%struct.Boxed* %right)
 %f.bool = fcmp oeq double %f.value.left, %f.value.right
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %f.bool)
 ret void
string.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 2)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 2)
 %left.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %left)
 %right.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %right)
 %strcmp = call i32 @strcmp(i8* %left.str, i8* %right.str)
 %s.bool = icmp eq i32 %strcmp, 0
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %s.bool)
 ret void
otherwise:
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 ret void
}

define void @SAME_NEQ(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 %type.left = call i3 @_GET_TYPE(%struct.Boxed* %left)
 switch i3 %type.left, label %otherwise [ i3 0, label %null.type
 i3 1, label %number.type
 i3 2, label %string.type ]
null.type:
 %type.right = call i3 @_GET_TYPE(%struct.Boxed* %right)
 switch i3 %type.right, label %otherwise [ i3 1, label %number.type
 i3 2, label %string.type ]
number.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 1)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 %f.value.left = call double @_GET_NUM_VALUE(%struct.Boxed* %left)
 %f.value.right = call double @_GET_NUM_VALUE(%struct.Boxed* %right)
 %f.bool = fcmp one double %f.value.left, %f.value.right
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %f.bool)
 ret void
string.type:
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 2)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 2)
 %left.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %left)
 %right.str = call i8* @_GET_STR_VALUE(%struct.Boxed* %right)
 %strcmp = call i32 @strcmp(i8* %left.str, i8* %right.str)
 %s.bool = icmp ne i32 %strcmp, 0
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %s.bool)
 ret void
otherwise:
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 1)
 ret void
}

; Equality (and inequality) is defined separaly because it may accept operands of different types.
; In every other way it is the same as OVERLOADED_CMP.
; TODO: make it work with null values.
define void @EQ(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 %type.left = call i3 @_GET_TYPE(%struct.Boxed* %left)
 %type.right = call i3 @_GET_TYPE(%struct.Boxed* %right)
 %are.same = icmp eq i3 %type.left, %type.right
 br i1 %are.same, label %yes.same, label %no.same
yes.same:
 call void @SAME_EQ(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right)
 ret void
no.same:
 %is.left.str = call i1 @_CHECK_TYPE(%struct.Boxed* %left, i3 2)
 %is.left.num = call i1 @_CHECK_TYPE(%struct.Boxed* %left, i3 1)
 %is.right.str = call i1 @_CHECK_TYPE(%struct.Boxed* %right, i3 2)
 %is.right.num = call i1 @_CHECK_TYPE(%struct.Boxed* %right, i3 1)
 %is.left.valid = or i1 %is.left.str, %is.left.num
 %is.right.valid = or i1 %is.right.str, %is.right.num
 %are.both.valid = and i1 %is.left.valid, %is.right.valid
 br i1 %are.both.valid, label %yes.valid, label %no.valid
yes.valid:
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 0)
 ret void
no.valid:
 call void @_NUM_OR_STR_E(%struct.Boxed* %left)
 call void @_NUM_OR_STR_E(%struct.Boxed* %right)
 ret void
}
define void @NEQ(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 %type.left = call i3 @_GET_TYPE(%struct.Boxed* %left)
 %type.right = call i3 @_GET_TYPE(%struct.Boxed* %right)
 %are.same = icmp eq i3 %type.left, %type.right
 br i1 %are.same, label %yes.same, label %no.same
yes.same:
 call void @SAME_NEQ(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right)
 ret void
no.same:
 %is.left.str = call i1 @_CHECK_TYPE(%struct.Boxed* %left, i3 2)
 %is.left.num = call i1 @_CHECK_TYPE(%struct.Boxed* %left, i3 1)
 %is.right.str = call i1 @_CHECK_TYPE(%struct.Boxed* %right, i3 2)
 %is.right.num = call i1 @_CHECK_TYPE(%struct.Boxed* %right, i3 1)
 %is.left.valid = or i1 %is.left.str, %is.left.num
 %is.right.valid = or i1 %is.right.str, %is.right.num
 %are.both.valid = and i1 %is.left.valid, %is.right.valid
 br i1 %are.both.valid, label %yes.valid, label %no.valid
yes.valid:
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 1)
 ret void
no.valid:
 call void @_NUM_OR_STR_E(%struct.Boxed* %left)
 call void @_NUM_OR_STR_E(%struct.Boxed* %right)
 ret void
}
; Arguments may be null, arguments must be booleans
define void @AND(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 3)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 3)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 3)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 3)
 %bool.left = call i1 @_GET_BOOL_VALUE(%struct.Boxed* %left)
 %bool.right = call i1 @_GET_BOOL_VALUE(%struct.Boxed* %right)
 %b.res = and i1 %bool.left, %bool.right
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %b.res)
 ret void
}

define void @OR(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 3)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 3)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 3)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 3)
 %bool.left = call i1 @_GET_BOOL_VALUE(%struct.Boxed* %left)
 %bool.right = call i1 @_GET_BOOL_VALUE(%struct.Boxed* %right)
 %b.res = or i1 %bool.left, %bool.right
 call void @_SET_BOOL_VALUE(%struct.Boxed* %res, i1 %b.res)
 ret void
}

;-02---- END BOOL.LL -------------------------------------------------------------------------------;
;-03---- BEGIN STRING.LL ---------------------------------------------------------------------------;
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~;
; Functions to deal with STRINGS ;
; Inspired by https:
; strings ;
; Strings are immutable, but there is no gc ;
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~;
; takes the ownership of the string
define void @_SET_STR_VALUE(%struct.Boxed* %self, i8* %value) nounwind {
 %type.ptr = getelementptr %struct.Boxed, %struct.Boxed* %self, i32 0, i32 0 ; extract the pointer to the bool from the struct
 %value.ptr = getelementptr %struct.Boxed, %struct.Boxed* %self, i32 0, i32 1 ; extract the pointer to the bool from the struct
 %s.value = ptrtoint i8* %value to i64 ; cast to a i64
 store i3 2, i3* %type.ptr ; insert the type in the result
 store i64 %s.value, i64* %value.ptr ; insert the value in the result
 ret void
}
define i8* @_GET_STR_VALUE(%struct.Boxed* %this) nounwind {
 %value.ptr = getelementptr %struct.Boxed, %struct.Boxed* %this, i32 0, i32 1 ; extract the pointer to the string from the struct
 %i.value = load i64, i64* %value.ptr ; extract the string pointer from the pointer
 %s.value = inttoptr i64 %i.value to i8* ; cast to a double
 ret i8* %s.value
}
define void @CONCAT(%struct.Boxed* %res, %struct.Boxed* %left, %struct.Boxed* %right) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %left, i3 2)
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %right, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %left, i3 2)
 call void @_CHECK_TYPE_E(%struct.Boxed* %right, i3 2)
 %left.string = call i8* @_GET_STR_VALUE(%struct.Boxed* %left)
 %right.string = call i8* @_GET_STR_VALUE(%struct.Boxed* %right)
 %len.left = call i32 @strlen(i8* %left.string)
 %len.right = call i32 @strlen(i8* %right.string)
 %len.new.0 = add i32 %len.left, %len.right
 %len.new = add i32 %len.new.0, 1 ; the new length is length(`left') + `length(`right') + 1 (null terminating byte)
 %new.string = call i8* @malloc(i32 %len.new) ; memory leak
 call i8* @strcpy(i8* %new.string, i8* %left.string)
 call i8* @strcat(i8* %new.string, i8* %right.string)
 call void @_SET_STR_VALUE(%struct.Boxed* %res, i8* %new.string)
 ret void
}
;-03---- END STRING.LL -----------------------------------------------------------------------------;
;-04---- BEGIN IO.LL -------------------------------------------------------------------------------
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
; Wrappers to library calls for I/O
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
@number.message = constant [6 x i8] c"%.2f\0A\00"
@string.message = constant [4 x i8] c"%s\0A\00"
@true.message = constant [6 x i8] c"true\0A\00"
@false.message = constant [7 x i8] c"false\0A\00"
@stdin = external global ptr, align 8
; Read a newline terminated line from stdin, the line must have less than 100 chars
define void @IO.ReadLine(%struct.Boxed* %this) {
 %new.string = call i8* @malloc(i32 100) ; memory leak
   %stdin = load ptr, ptr @stdin, align 8
   call ptr @fgets(ptr noundef %new.string, i32 100, ptr noundef %stdin)
 %strlen.0 = call i32 @strlen(i8* %new.string)
 %strlen.1 = sub i32 %strlen.0, 1
 %last.char.ptr = getelementptr i8, i8* %new.string, i32 %strlen.1
 store i8 0, i8* %last.char.ptr ; replace newline with null
 call void @_SET_STR_VALUE(%struct.Boxed* %this, i8* %new.string)
 ret void
}
; Print a box to stdout.
; the type must not be an array.
; `value' must not be a NULL (TODO: maybe add a specific clause for printing null)
define void @IO.WriteLine(%struct.Boxed* %null, %struct.Boxed* %value) {
 %type = call i3 @_GET_TYPE(%struct.Boxed* %value)
 switch i3 %type, label %otherwise [ i3 1, label %number.type
                                     i3 2, label %str.type
                i3 3, label %bool.type ]
number.type:
 %f.value = call double @_GET_NUM_VALUE(%struct.Boxed* %value)
 call i32 (i8*, ...) @printf(i8* getelementptr([6 x i8], [6 x i8]* @number.message, i32 0, i32 0), double %f.value)
 ret void
str.type:
 %s.value = call i8* @_GET_STR_VALUE(%struct.Boxed* %value)
 call i32 (i8*, ...) @printf(i8* getelementptr([4 x i8], [4 x i8]* @string.message, i32 0, i32 0), i8* %s.value)
 ret void
bool.type:
 %b.value = call i1 @_GET_BOOL_VALUE(%struct.Boxed* %value)
 br i1 %b.value, label %print.true, label %print.false
print.true:
 call i32 (i8*, ...) @printf(i8* getelementptr([6 x i8], [6 x i8]* @true.message, i32 0, i32 0))
 ret void
print.false:
 call i32 (i8*, ...) @printf(i8* getelementptr([7 x i8], [7 x i8]* @false.message, i32 0, i32 0))
 ret void
otherwise:
 switch i3 %type, label %unknown.type [ i3 4, label %array.type ]
array.type:
 call void @_ARRAY_PRINT_E()
 ret void
unknown.type:
 call void @_UNKNOWN_ERROR()
 ret void
}
;-04---- END IO.LL ---------------------------------------------------------------------------------
;-05---- BEGIN MATH.LL -----------------------------------------------------------------------------
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
; Calls to math functions
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
; import a specific math function
declare double @llvm.cos.f64(double)

declare double @llvm.sin.f64(double)

declare double @llvm.log.f64(double)

declare double @llvm.sqrt.f64(double)

declare double @llvm.floor.f64(double)

; call a specific imported math function on a box
; the box must be a number, but may be null
define void @Math.Cos(%struct.Boxed* %res, %struct.Boxed* %value) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %value, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %value, i3 1)
 %f.value.0 = call double @_GET_NUM_VALUE(%struct.Boxed* %value)
 %f.value.1 = call double @llvm.cos.f64(double %f.value.0)
 call void @_SET_NUM_VALUE(%struct.Boxed* %res, double %f.value.1)
 ret void
}

define void @Math.Sin(%struct.Boxed* %res, %struct.Boxed* %value) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %value, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %value, i3 1)
 %f.value.0 = call double @_GET_NUM_VALUE(%struct.Boxed* %value)
 %f.value.1 = call double @llvm.sin.f64(double %f.value.0)
 call void @_SET_NUM_VALUE(%struct.Boxed* %res, double %f.value.1)
 ret void
}

define void @Math.Sqrt(%struct.Boxed* %res, %struct.Boxed* %value) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %value, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %value, i3 1)
 %f.value.0 = call double @_GET_NUM_VALUE(%struct.Boxed* %value)
 %f.value.1 = call double @llvm.sqrt.f64(double %f.value.0)
 call void @_SET_NUM_VALUE(%struct.Boxed* %res, double %f.value.1)
 ret void
}

define void @Math.Log(%struct.Boxed* %res, %struct.Boxed* %value) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %value, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %value, i3 1)
 %f.value.0 = call double @_GET_NUM_VALUE(%struct.Boxed* %value)
 %f.value.1 = call double @llvm.log.f64(double %f.value.0)
 call void @_SET_NUM_VALUE(%struct.Boxed* %res, double %f.value.1)
 ret void
}

define void @Math.Floor(%struct.Boxed* %res, %struct.Boxed* %value) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %value, i3 1)
 call void @_CHECK_TYPE_E(%struct.Boxed* %value, i3 1)
 %f.value.0 = call double @_GET_NUM_VALUE(%struct.Boxed* %value)
 %f.value.1 = call double @llvm.floor.f64(double %f.value.0)
 call void @_SET_NUM_VALUE(%struct.Boxed* %res, double %f.value.1)
 ret void
}

;-05---- END MATH.LL -------------------------------------------------------------------------------
;-06---- BEGIN ERROR.LL ----------------------------------------------------------------------------
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
; Runtime error routines
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
@number.type.string = constant [7 x i8] c"NUMBER\00"
@string.type.string = constant [7 x i8] c"STRING\00"
@bool.type.string = constant [5 x i8] c"BOOL\00"
@array.type.string = constant [6 x i8] c"ARRAY\00"
@unknown.type.string = constant [8 x i8] c"UNKNOWN\00"
@type.error.message = constant [58 x i8] c"*** Runtime exception: expected %s, but got %s (line %d)\0A\00"
@zero.div.message = constant [54 x i8] c"*** Runtime exception: zero division error (line %d)\0A\00"
@unknown.error = constant [48 x i8] c"*** Runtime exception: unknown error (line %d)\0A\00"
@line.number = global i32 0
; return a boolean saying whether a box has type `expected' or not.
define i1 @_CHECK_TYPE(%struct.Boxed* %value, i3 %expected) {
 %type = call i3 @_GET_TYPE(%struct.Boxed* %value)
 %ret = icmp eq i3 %type, %expected
 ret i1 %ret
}
; return the string represetation of a type.
define i8* @_GET_TYPE_REPR(i3 %type) {
 switch i3 %type, label %otherwise [ i3 1, label %number.type
                                     i3 2, label %str.type
                i3 3, label %bool.type ]
number.type:
 %number.msg = getelementptr [7 x i8], [7 x i8]* @number.type.string, i32 0, i32 0
 br label %print
str.type:
 %str.msg = getelementptr [7 x i8], [7 x i8]* @string.type.string, i32 0, i32 0
 br label %print
bool.type:
 %bool.msg = getelementptr [5 x i8], [5 x i8]* @bool.type.string, i32 0, i32 0
 br label %print
otherwise:
 switch i3 %type, label %unknown.type [ i3 4, label %array.type ]
array.type:
 %array.msg = getelementptr [7 x i8], [7 x i8]* @array.type.string, i32 0, i32 0
 br label %end.otherwise
unknown.type:
 %unknown.msg = getelementptr [8 x i8], [8 x i8]* @unknown.type.string, i32 0, i32 0
 br label %end.otherwise
end.otherwise:
 %otherwise.msg = phi i8* [%array.msg, %array.type], [%unknown.msg, %unknown.type]
 br label %print
print:
 %msg = phi i8* [%number.msg, %number.type], [%str.msg, %str.type], [%bool.msg, %bool.type], [%otherwise.msg, %end.otherwise]
 ret i8* %msg
}
; throw a generic error
define void @_UNKNOWN_ERROR() {
 %line = load i32, i32* @line.number
 call i32 (i8*, ...) @printf(i8* getelementptr([47 x i8], [47 x i8]* @unknown.error, i32 0, i32 0), i32 %line)
 call void @abort()
 ret void
}
; throw a zero division error if `value' is zero
define void @_CHECK_ZERO_DIV_E(%struct.Boxed* %value) {
 %num = call double @_GET_NUM_VALUE(%struct.Boxed* %value)
 %is.zero = fcmp oeq double %num, 0.0
 br i1 %is.zero, label %true, label %false
true:
 %line = load i32, i32* @line.number
 call i32 (i8*, ...) @printf(i8* getelementptr([54 x i8], [54 x i8]* @zero.div.message, i32 0, i32 0), i32 %line)
 call void @abort()
 ret void
false:
 ret void
}
; throw a type error if `value''s type is different from `expected'
define void @_CHECK_TYPE_E(%struct.Boxed* %value, i3 %expected) {
 %type = call i3 @_GET_TYPE(%struct.Boxed* %value)
 %are.equal = icmp eq i3 %type, %expected
 br i1 %are.equal, label %end, label %throw.exception
throw.exception:
 %actual.str = call i8* @_GET_TYPE_REPR(i3 %type)
 %expected.str = call i8* @_GET_TYPE_REPR(i3 %expected)
 %line = load i32, i32* @line.number
 call i32 (i8*, ...) @printf(i8* getelementptr([58 x i8], [58 x i8]* @type.error.message, i32 0, i32 0), i8* %expected.str, i8* %actual.str, i32 %line )
 call void @abort()
 ret void
end:
 ret void
}
; throw a negative index error if `index' is less than zero
@negative.index.msg = constant [57 x i8] c"*** Runtime exception: %d is a negative index (line %d)\0A\00"
define void @_CHECK_POSITIVE_INDEX_E(i32 %index) {
 %is.negative = icmp slt i32 %index, 0
 br i1 %is.negative, label %true, label %false
true:
 %line = load i32, i32* @line.number
 call i32 (i8*, ...) @printf(i8* getelementptr([57 x i8], [57 x i8]* @negative.index.msg, i32 0, i32 0), i32 %index, i32 %line)
 call void @abort()
 ret void
false:
 ret void
}
; throw an array copy error if the user is trying to copy an array
@array.copy.msg = constant [75 x i8] c"*** Runtime exception: array copy (<arr> = <arr>) not supported (line %d)\0A\00"
define void @_ARRAY_COPY_E() {
 %line = load i32, i32* @line.number
 call i32 (i8*, ...) @printf(i8* getelementptr([75 x i8], [75 x i8]* @array.copy.msg, i32 0, i32 0), i32 %line )
 call void @abort()
 ret void
}
; throw an array print error if the user is trying to print an array
@array.print.msg = constant [63 x i8] c"*** Runtime exception: array printing not supported (line %d)\0A\00"
define void @_ARRAY_PRINT_E() {
 %line = load i32, i32* @line.number
 call i32 (i8*, ...) @printf(i8* getelementptr([63 x i8], [63 x i8]* @array.print.msg, i32 0, i32 0), i32 %line )
 call void @abort()
 ret void
}
; throw an error if the input has not type STRING or NUM
@str.or.num.msg = constant [93 x i8] c"*** Runtime exception: expected a value of type NUMBER or STRING, instead got %s at line %d\0A\00"
define void @_NUM_OR_STR_E(%struct.Boxed* %this) {
 %type = call i3 @_GET_TYPE(%struct.Boxed* %this)
 switch i3 %type, label %problem[ i3 1, label %no.problem
                                  i3 2, label %no.problem ]
problem:
 %line = load i32, i32* @line.number
 %type.repr = call i8* @_GET_TYPE_REPR(i3 %type)
 call i32 (i8*, ...) @printf(i8* getelementptr([93 x i8], [93 x i8]* @str.or.num.msg, i32 0, i32 0), i8* %type.repr, i32 %line)
 call void @abort()
 ret void
no.problem:
 ret void
}
;-04---- END ERROR.LL ------------------------------------------------------------------------------
;-07---- BEGIN ARRAY.LL ----------------------------------------------------------------------------
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
; Arrays
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
%struct.Array = type {
 i32,
 %struct.Boxed*
}
; sizeof(Boxed)
@box.size = constant i32 ptrtoint (%struct.Boxed* getelementptr (%struct.Boxed, %struct.Boxed* null, i32 1) to i32)
; sizoef(Array)
@array.size = constant i32 ptrtoint (%struct.Array* getelementptr (%struct.Array, %struct.Array* null, i32 1) to i32)
; init a Boxed valuue to an empty array
; @param this the struct.Boxed to be initialized to an empty array
define void @_EMPTY_ARRAY(%struct.Boxed* %this) {
 %type.ptr = getelementptr %struct.Boxed, %struct.Boxed* %this, i32 0, i32 0
 %value.ptr = getelementptr %struct.Boxed, %struct.Boxed* %this, i32 0, i32 1
 %array.size = load i32, i32* @array.size ; get the size of an array
 %empty.arr.bytes = call i8* @malloc(i32 %array.size) ; allocate a <size> number of bytes
 %empty.arr = bitcast i8* %empty.arr.bytes to %struct.Array* ; cast the pointer to bytes to one to an array
 call void @_SET_CAPACITY(%struct.Array* %empty.arr, i32 0)
 call void @_SET_CONTENTS(%struct.Array* %empty.arr, %struct.Boxed* null) ; init to null the contetns
 store i3 4, i3* %type.ptr
 %value = ptrtoint %struct.Array* %empty.arr to i64
 store i64 %value, i64* %value.ptr
 ret void
}
; These five functions are intended to be used inside this file
define i32 @_GET_CAPACITY(%struct.Array* %this) {
 %capacity.ptr = getelementptr %struct.Array, %struct.Array* %this, i32 0, i32 0
 %capacity = load i32, i32* %capacity.ptr
 ret i32 %capacity
}
define %struct.Boxed* @_GET_CONTENTS(%struct.Array* %this) {
 %array.ptr = getelementptr %struct.Array, %struct.Array* %this, i32 0, i32 1
 %box = load %struct.Boxed*, %struct.Boxed** %array.ptr
 ret %struct.Boxed* %box
}
define void @_SET_CAPACITY(%struct.Array* %this, i32 %new) {
 %capacity.ptr = getelementptr %struct.Array, %struct.Array* %this, i32 0, i32 0
 store i32 %new, i32* %capacity.ptr
 ret void
}
define void @_SET_CONTENTS(%struct.Array* %this, %struct.Boxed* %new) {
 %array.ptr = getelementptr %struct.Array, %struct.Array* %this, i32 0, i32 1
 store %struct.Boxed* %new, %struct.Boxed** %array.ptr
 ret void
}
define %struct.Array* @_GET_ARRAY(%struct.Boxed* %this) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %this, i3 4) ; if this is null, the default it to a boxed array
 call void @_CHECK_TYPE_E(%struct.Boxed* %this, i3 4) ; else if the type of this is not i3 4 throw an exception
 %arr.ptr = getelementptr %struct.Boxed, %struct.Boxed* %this, i32 0, i32 1
 %i.arr = load i64, i64* %arr.ptr
 %arr = inttoptr i64 %i.arr to %struct.Array*
 ret %struct.Array* %arr
}
; get the index th element out of this boxed array.
; if the array is smaller than the index the contents are expanded
; @param this the box, must be NULL or an array box or else an error is thrown
; @param index the index, must be NULL or of NUMBER type
; @returns the index-th element of this
define %struct.Boxed* @_GET_ARRAY_ELEMENT(%struct.Boxed* %this, %struct.Boxed* %index) {
 call void @_DEFAULT_IF_NULL(%struct.Boxed* %this, i3 4) ; if (this is null) then default(this)
 call void @_CHECK_TYPE_E(%struct.Boxed* %this, i3 4) ; assert(this.type == ARRAY)
 %i.index = call i32 @_FLOOR(%struct.Boxed* %index) ; i = floor(index)
 call void @_CHECK_POSITIVE_INDEX_E(i32 %i.index) ; assert(i >= 0)
 %array = call %struct.Array* @_GET_ARRAY(%struct.Boxed* %this) ; array = this.array
 %capacity = call i32 @_GET_CAPACITY(%struct.Array* %array) ; capacity = array.capacity
 %contents = call %struct.Boxed* @_GET_CONTENTS(%struct.Array* %array) ; contents = array.contents
 %is.smaller = icmp slt i32 %i.index, %capacity ; b = i < capacity
 br i1 %is.smaller, label %true, label %false ;
true:
 %struct.ptr = getelementptr %struct.Boxed, %struct.Boxed* %contents, i32 %i.index
 ret %struct.Boxed* %struct.ptr
false:
 call void @_EXPAND(%struct.Array* %array, i32 %i.index)
 %ret = call %struct.Boxed* @_GET_ARRAY_ELEMENT(%struct.Boxed* %this, %struct.Boxed* %index)
 ret %struct.Boxed* %ret
}
define void @_EXPAND(%struct.Array* %this, i32 %index) {
 %contents.ptr = call %struct.Boxed* @_GET_CONTENTS(%struct.Array* %this)
 %old.capacity = call i32 @_GET_CAPACITY(%struct.Array* %this)
 %i8.ptr.arr = bitcast %struct.Boxed* %contents.ptr to i8* ; cast the pointer to a byte pointer
 %box.size = load i32, i32* @box.size ; sizeof(Boxed)
 %new.number.of.elements = add i32 %index, 1 ; new-capacity = index + 1
 %new.size = mul i32 %box.size, %new.number.of.elements ; bytes-to-alloc = new-capacity * siezeof(Boxed)
 %new.contents.bytes = call i8* @realloc(i8* %i8.ptr.arr, i32 %new.size) ; realloc, this may cause bugs since the memory is uninitialized, and the use may access a bad-box
 %new.contents = bitcast i8* %new.contents.bytes to %struct.Boxed* ; cast back to a [Boxed]
 %i = alloca i32 ; i = old-capacity
 store i32 %old.capacity, i32* %i
 br label %loop
check:
 %old.i.0 = load i32, i32* %i
 %is.bigger = icmp sge i32 %old.i.0, %index ; if (i >= index) break
 br i1 %is.bigger, label %end, label %loop
loop:
 %old.i.1 = load i32, i32* %i
 %struct.ptr = getelementptr %struct.Boxed, %struct.Boxed* %new.contents, i32 %old.i.1 ; arr[i]
 call void @_SET_TYPE(%struct.Boxed* %struct.ptr, i3 0) ; box[type] = NULL
 %old.i.2 = add i32 %old.i.1, 1 ; i++
 store i32 %old.i.2, i32* %i
 br label %check ; jump check
end:
 call void @_SET_CAPACITY(%struct.Array* %this, i32 %new.number.of.elements)
 call void @_SET_CONTENTS(%struct.Array* %this, %struct.Boxed* %new.contents)
 ret void
}
;-07---- END ARRAY.LL ------------------------------------------------------------------------------
;---------------------------------------------------------------------------------------------------;
;- END PRELUDE ~ BEGIN PROGRAM ---------------------------------------------------------------------;
;---------------------------------------------------------------------------------------------------;