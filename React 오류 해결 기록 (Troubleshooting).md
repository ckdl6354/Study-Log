# 🛠️ React Troubleshooting

React 교육 들으면서 발생한 오류와 원인, 해결 방법을 정리한 기록입니다.

---

## 1. Student.jsx - undefined (reading 'name')

### ❌ Error

```bash
Uncaught TypeError: Cannot read properties of undefined (reading 'name')
```

### 🔍 원인

Student 컴포넌트에서 객체의 `name` 속성에 접근했지만, 해당 객체가 `undefined`인 상태였다.

부모 컴포넌트에서 props를 전달하지 않았거나, 전달받은 데이터의 이름 또는 구조가 일치하지 않을 때 발생할 수 있다.

### ✅ 해결 방법

부모 컴포넌트에서 올바르게 데이터를 전달했는지 확인한다.

```jsx
// App.jsx
function App() {
  const student = {
    name: "홍길동"
  };

  return <Student student={student} />;
}
```

```jsx
// Student.jsx
function Student({ student }) {
  return <h1>{student?.name}</h1>;
}

export default Student;
```

`Optional Chaining (?.)`을 사용하면 객체가 `undefined` 또는 `null`인 경우에도 오류 없이 처리할 수 있다.

---

## 2. App3.jsx - default export 오류

### ❌ Error

```bash
Uncaught SyntaxError:
The requested module '/src/App3.jsx'
does not provide an export named 'default'
```

### 🔍 원인

`main.jsx`에서 `App3`를 default import 방식으로 불러왔지만, `App3.jsx`에서 default export를 하지 않아 발생한 오류이다.

### 문제 코드

```jsx
// main.jsx
import App3 from './App3.jsx';
```

```jsx
// App3.jsx
export function App3() {
  return <h1>App3</h1>;
}
```

### ✅ 해결 방법

**방법 1. Default Export 사용**

```jsx
// App3.jsx
function App3() {
  return <h1>App3</h1>;
}

export default App3;
```

**방법 2. Named Export 사용**

```jsx
// App3.jsx
export function App3() {
  return <h1>App3</h1>;
}
```

```jsx
// main.jsx
import { App3 } from './App3.jsx';
```

### 💡 핵심 정리

| 구분 | Export | Import |
|---|---|---|
| Default Export | `export default App3` | `import App3 from './App3'` |
| Named Export | `export { App3 }` | `import { App3 } from './App3'` |

Export 방식과 Import 방식은 반드시 일치해야 한다.

---

## 3. Corse.jsx - undefined (reading 'map')

### ❌ Error

```bash
Uncaught TypeError: Cannot read properties of undefined (reading 'map')
```

### 🔍 원인

`map()` 메서드는 배열에서 사용하는 메서드이다.
하지만 Corse 컴포넌트에서 `undefined` 상태인 데이터에 `map()`을 호출하여 오류가 발생했다.

비동기 통신(API 요청 등)으로 데이터를 가져오기 전 초기값이 배열이 아니거나, 부모 컴포넌트에서 props 배열을 올바르게 전달하지 않았을 때 자주 발생한다.

### 문제 코드

```jsx
// Corse.jsx
function Corse(props) {
  return (
    <ul>
      {props.courses.map((course) => (
        <li key={course.id}>{course.title}</li>
      ))}
    </ul>
  );
}
```

### ✅ 해결 방법

**방법 1. Optional Chaining (?.) 사용**

데이터가 로딩 중이거나 아직 비어있을 때(undefined) 에러를 뿜지 않고 렌더링을 안전하게 건너뛰도록 처리한다.

```jsx
function Corse(props) {
  return (
    <ul>
      {props.courses?.map((course) => (
        <li key={course.id}>{course.title}</li>
      ))}
    </ul>
  );
}
```

**방법 2. 단락 평가 (Short-circuit Evaluation) 활용**

`props.courses`가 존재할 때만 뒤의 `map()`이 실행되도록 만든다.

```jsx
function Corse(props) {
  return (
    <ul>
      {props.courses && props.courses.map((course) => (
        <li key={course.id}>{course.title}</li>
      ))}
    </ul>
  );
}
```

**방법 3. 기본값(Default Parameter) 지정**

구조 분해 할당을 사용해 props가 넘어오지 않거나 비어있을 때 빈 배열(`[]`)이 기본값으로 들어가도록 설정한다.

```jsx
function Corse({ courses = [] }) {
  return (
    <ul>
      {courses.map((course) => (
        <li key={course.id}>{course.title}</li>
      ))}
    </ul>
  );
}
```
