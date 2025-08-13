# PythonBackend 프로젝트 : Poetry 기반 Python 프로젝트 관리

## 1. Poetry 설치

Poetry는 Python 프로젝트의 의존성 관리 및 패키징을 위한 도구입니다. 아래 명령어로 Poetry를 설치합니다.

Linux, macOS, Windows (WSL)
```bash
curl -sSL https://install.python-poetry.org | python3 -
```

Windows (Powershell)
```bash
(Invoke-WebRequest -Uri https://install.python-poetry.org -UseBasicParsing).Content | py -
```

설치 후, Poetry 명령어를 사용할 수 있도록 환경 변수에 경로를 추가합니다.

```bash
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

윈도우

시스템 환경 변수 편집 -> 환경 변수 -> 시스템 변수 Path 추가 -> C:\Users\Playdata\AppData\Roaming\Python\Scripts 

설치 확인:

```bash
poetry --version
```


## 2. 의존성 관리

필요한 패키지를 추가하려면 다음과 같이 입력합니다.

```bash
poetry add fastapi uvicorn pydantic
```

개발 의존성(예: 테스트, 린터 등)은 `--dev` 플래그를 사용하여 추가합니다.

```bash
poetry add --dev pytest black flake8
```

## 3. 가상환경 활성화 및 서버 실행

Poetry는 프로젝트마다 격리된 가상환경을 자동으로 생성합니다. 서버를 실행하려면 다음 명령어를 사용합니다.

```bash
poetry run uvicorn app.main:app --reload
```

가상환경에 직접 진입하려면:

```bash
poetry shell
```

## 4. 버전 고정 및 재현성 보장

Poetry는 `pyproject.toml`과 `poetry.lock` 파일을 통해 의존성 버전을 고정하고, 팀원 간 동일한 환경을 보장합니다. 새로운 의존성을 추가하거나 업데이트할 때마다 `poetry.lock` 파일이 자동으로 갱신됩니다.


##  5. 추가 리소스

* [Poetry 공식 문서](https://python-poetry.org/docs/)
* [Python Poetry in 8 Minutes - YouTube](https://www.youtube.com/watch?v=Ji2XDxmXSOM)

