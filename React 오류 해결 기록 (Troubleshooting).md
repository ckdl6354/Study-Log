React 오류 해결 기록 (Troubleshooting)
1. Student.jsx - Cannot read properties of undefined (reading 'name')
🔴 문제 상황
bash
Student.jsx:4 Uncaught TypeError:
Cannot read properties of undefined (reading 'name')


Student 컴포넌트의 4번째 줄에서 undefined인 객체의 name 속성에 접근하면서 오류가 발생했다.

🔍 원인

객체가 전달되지 않았거나, props로 전달받은 데이터의 구조가 예상과 다를 때 발생하는 오류이다.

예를 들어 다음과 같은 코드에서 문제가 발생할 수 있다.

jsx
function Student(props) {
  return <h1>{props.student.name}</h1>;
}


props.student가 undefined라면 .name에 접근할 수 없다.

✅ 해결 방법

방법 1. 부모 컴포넌트에서 props 전달 확인

jsx
function App() {
  const student = {
    name: "홍길동"
  };

  return <Student student={student} />;
}


방법 2. Optional Chaining 사용

jsx
function Student(props) {
  return <h1>{props.student?.name}</h1>;
}


데이터가 전달되지 않은 경우에도 오류가 발생하지 않도록 처리할 수 있다.

2. App3.jsx - does not provide an export named 'default'
🔴 문제 상황
bash
Uncaught SyntaxError:
The requested module '/src/App3.jsx'
does not provide an export named 'default'


main.jsx에서 App3.jsx를 default import 방식으로 불러왔지만, 해당 파일에서 default export를 제공하지 않아 발생한 오류이다.

🔍 원인

main.jsx

jsx
import App3 from './App3.jsx';


위와 같이 작성했다면 App3.jsx 파일에서 반드시 default export가 있어야 한다.

하지만 다음과 같이 작성되어 있다면 오류가 발생한다.

jsx
export function App3() {
  return <h1>App3</h1>;
}

✅ 해결 방법

방법 1. default export 사용

jsx
function App3() {
  return <h1>App3</h1>;
}

export default App3;


방법 2. Named Export 방식 사용

jsx
export function App3() {
  return <h1>App3</h1>;
}


이 경우 import도 다음과 같이 수정해야 한다.

jsx
import { App3 } from './App3.jsx';

💡 핵심 정리
export default → 중괄호 없이 import
export → 중괄호를 사용하여 import
3. Corse.jsx - Cannot read properties of undefined (reading 'map')
🔴 문제 상황
bash
Corse.jsx:5 Uncaught TypeError:
Cannot read properties of undefined (reading 'map')


Corse.jsx의 5번째 줄에서 map() 메서드를 호출했지만, 호출 대상이 undefined여서 발생한 오류이다.

🔍 원인

map()은 배열에서 사용할 수 있는 메서드이다.

따라서 배열이 아닌 undefined에 map()을 호출하면 오류가 발생한다.

예를 들어 다음과 같은 코드에서 문제가 발생할 수 있다.

jsx
function Corse(props) {
  return (
    <div>
      {props.courses.map((course) => (
        <p>{course}</p>
      ))}
    </div>
  );
}


부모 컴포넌트에서 courses 배열을 전달하지 않았다면 오류가 발생한다.

✅ 해결 방법

방법 1. 부모 컴포넌트에서 배열 전달 확인

jsx
function App3() {
  const courses = ["HTML", "CSS", "JavaScript", "React"];

  return <Corse courses={courses} />;
}


방법 2. 기본값 설정

jsx
function Corse({ courses = [] }) {
  return (
    <div>
      {courses.map((course, index) => (
        <p key={index}>{course}</p>
      ))}
    </div>
  );
}


배열이 전달되지 않더라도 기본값인 빈 배열을 사용하기 때문에 오류가 발생하지 않는다.

4. 전체 오류 정리
파일	오류 내용	해결 방법
Student.jsx	undefined 객체의 name 접근	props 전달 확인 및 Optional Chaining
App3.jsx	default export 누락	export와 import 방식 일치시키기
Corse.jsx	undefined에 map() 호출	배열 전달 확인 및 기본값 설정
5. 배운 점 (느낀 점)

이번 오류들을 해결하면서 React에서 컴포넌트 간 데이터 전달과 모듈 내보내기 및 가져오기 방식이 중요하다는 것을 알게 되었다.

특히 다음 세 가지를 기억해야겠다.

Props 확인: 자식 컴포넌트에서 데이터를 사용할 때 부모 컴포넌트가 올바르게 전달했는지 확인하기.
Export / Import 확인: default export와 named export의 문법 차이를 이해하고 일치시키기.
배열 데이터 확인: map()을 사용하기 전에 해당 데이터가 실제 배열인지 확인하기.

앞으로 React 개발 중 오류가 발생하면 콘솔 메시지와 오류가 발생한 파일 및 줄 번호를 먼저 확인하고, 데이터 전달 과정부터 점검해야겠다.
