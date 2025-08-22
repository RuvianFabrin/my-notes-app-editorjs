# My Notes App - Editor de Notas Android com Funcionalidades EditorJS

Uma aplicação Android nativa que replica as funcionalidades do EditorJS, permitindo criar e editar notas com blocos estruturados, pesquisa avançada, ordenação e formatação de texto com cores.

## 🚀 Funcionalidades

### ✨ Editor de Blocos (Inspirado no EditorJS)
- **Parágrafo**: Texto simples com formatação
- **Cabeçalho**: H1, H2, H3 com tamanhos diferentes
- **Citação**: Blocos de citação com autor/fonte
- **Lista**: Listas ordenadas e não-ordenadas
- **Código**: Blocos de código com fonte monoespaçada
- **Aviso**: Blocos de alerta/aviso destacados
- **Delimitador**: Separadores visuais

### 🎨 Formatação de Texto
- **Cor do Texto**: Múltiplas cores disponíveis (vermelho, azul, verde, roxo, etc.)
- **Cor de Fundo**: Destaque com cores de fundo (amarelo, verde claro, azul claro, etc.)
- **Seleção de Texto**: Aplicação de cores em texto selecionado
- **Interface Intuitiva**: Botões de formatação fáceis de usar

### 🔍 Pesquisa e Organização
- **Pesquisa por Título**: Busca em tempo real nos títulos das notas
- **Pesquisa por Tags**: Localização por tags personalizadas
- **Pesquisa por Conteúdo**: Busca no conteúdo completo das notas
- **Ordenação Múltipla**:
  - Por título (A-Z / Z-A)
  - Por data de atualização (mais recente / mais antiga)
  - Por data de criação (mais recente / mais antiga)

### 💾 Armazenamento
- **SQLite Local**: Dados salvos localmente no dispositivo
- **Formato EditorJS**: Conteúdo salvo em JSON compatível com EditorJS
- **Backup/Restore**: Estrutura preparada para backup dos dados
- **Exportação**: Possibilidade de exportar notas em formato JSON

## 🏗️ Arquitetura

### Padrão MVVM
- **Models**: `Note`, `EditorBlock`, `EditorJSData`
- **Repository**: `NoteRepository` para abstração de dados
- **Database**: `NotesDatabaseHelper` com SQLite
- **Utils**: Conversores JSON e formatadores de cor

### Componentes Principais
```
app/
├── models/
│   ├── Note.kt                    # Modelo de dados da nota
│   └── EditorBlock.kt             # Modelo de blocos do editor
├── utils/
│   ├── EditorJSJsonConverter.kt   # Conversão para formato EditorJS
│   └── ColorFormatter.kt          # Formatação de cores no texto
├── adapters/
│   ├── NoteListAdapter.kt         # Lista de notas
│   ├── EditorRecyclerViewAdapter.kt # Editor de blocos
│   ├── ListItemAdapter.kt         # Itens de lista
│   └── ColorPickerAdapter.kt      # Seletor de cores
├── activities/
│   ├── MainActivity.kt            # Tela principal
│   └── NoteEditorActivity.kt      # Editor de notas
├── NoteRepository.kt              # Repositório de dados
└── NotesDatabaseHelper.kt         # Helper do SQLite
```

## 🎯 Funcionalidades Técnicas

### Compatibilidade EditorJS
- **Formato JSON**: Estrutura idêntica ao EditorJS
- **Blocos Suportados**: Todos os tipos principais do EditorJS
- **Versionamento**: Compatível com EditorJS v2.28.2
- **Importação/Exportação**: JSON válido para uso em outras plataformas

### Performance
- **RecyclerView**: Listas otimizadas para performance
- **Coroutines**: Operações assíncronas para UI responsiva
- **Lazy Loading**: Carregamento eficiente de dados
- **Memory Management**: Gestão adequada de memória

### UI/UX Moderno
- **Material Design 3**: Interface seguindo guidelines do Google
- **Cores Personalizadas**: Esquema de cores moderno e limpo
- **Tipografia**: Fontes legíveis e hierarquia visual clara
- **Responsividade**: Adaptação a diferentes tamanhos de tela
- **Animações Sutis**: Transições suaves entre telas

## 📱 Requisitos do Sistema

- **Android API 21+** (Android 5.0 Lollipop)
- **Kotlin 1.8.20**
- **Gradle 8.1.0**
- **Material Design Components**

## 🛠️ Dependências Principais

```gradle
dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.10.1'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    
    // UI Components
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.0'
    
    // Architecture Components
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.1'
    
    // JSON Processing
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

## 🚀 Como Usar

### Criando uma Nova Nota
1. Toque no botão "+" (FAB) na tela principal
2. Digite o título da nota
3. Adicione tags (opcional)
4. Use o botão "+ Bloco" para adicionar diferentes tipos de conteúdo
5. Formate o texto selecionando e usando os botões de cor
6. Toque em "Salvar" para salvar a nota

### Editando Notas Existentes
1. Toque em uma nota na lista ou use o botão "Editar"
2. Modifique o conteúdo conforme necessário
3. Use as ferramentas de formatação para destacar texto
4. Salve as alterações

### Pesquisando Notas
1. Use a barra de pesquisa no topo da tela principal
2. Digite palavras-chave do título, tags ou conteúdo
3. Os resultados são filtrados em tempo real

### Organizando Notas
1. Toque no botão "Ordenar" para escolher critério de ordenação
2. Selecione entre título ou data (crescente/decrescente)
3. A lista é reorganizada automaticamente

## 🔧 Configuração de Desenvolvimento

### Pré-requisitos
- Android Studio Arctic Fox ou superior
- JDK 8 ou superior
- Android SDK API 21+

### Instalação
1. Clone o repositório
2. Abra o projeto no Android Studio
3. Sincronize as dependências do Gradle
4. Execute o projeto em um emulador ou dispositivo

### Estrutura de Banco de Dados
```sql
CREATE TABLE notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,  -- JSON no formato EditorJS
    tags TEXT,              -- Tags separadas por vírgula
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

## 🎨 Personalização

### Adicionando Novas Cores
Edite `ColorFormatter.kt` para adicionar novas opções de cores:

```kotlin
val TEXT_COLORS = mapOf(
    "nova_cor" to Color.parseColor("#FF5722"),
    // ... outras cores
)
```

### Novos Tipos de Bloco
Para adicionar novos tipos de bloco, edite `EditorBlock.kt`:

```kotlin
companion object {
    const val TYPE_NOVO_BLOCO = "novo_bloco"
    
    fun createNovoBloco(texto: String = ""): EditorBlock {
        return EditorBlock(TYPE_NOVO_BLOCO, mapOf("text" to texto))
    }
}
```

## 📄 Formato EditorJS

As notas são salvas no formato JSON compatível com EditorJS:

```json
{
  "time": 1672531200000,
  "blocks": [
    {
      "type": "paragraph",
      "data": {
        "text": "Texto do parágrafo com <span style='color: red'>formatação</span>"
      }
    },
    {
      "type": "header",
      "data": {
        "text": "Título da Seção",
        "level": 2
      }
    }
  ],
  "version": "2.28.2"
}
```

## 🤝 Contribuição

Contribuições são bem-vindas! Por favor:

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 🔮 Roadmap

### Próximas Funcionalidades
- [ ] Sincronização em nuvem
- [ ] Compartilhamento de notas
- [ ] Modo escuro
- [ ] Backup automático
- [ ] Importação de arquivos Markdown
- [ ] Plugin system para novos tipos de bloco
- [ ] Colaboração em tempo real
- [ ] Criptografia de notas sensíveis

### Melhorias Planejadas
- [ ] Performance otimizada para grandes volumes de notas
- [ ] Busca com filtros avançados
- [ ] Categorização automática por IA
- [ ] Widget para tela inicial
- [ ] Integração com assistentes de voz

## 📞 Suporte

Para dúvidas, sugestões ou problemas:
- Abra uma issue no GitHub
- Entre em contato através do email do desenvolvedor

---

**My Notes App** - Transformando a experiência de criação de notas no Android com a flexibilidade do EditorJS! 📝✨
